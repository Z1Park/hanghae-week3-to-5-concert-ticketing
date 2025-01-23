package kr.hhplus.be.server.domain.reservation

import jakarta.transaction.Transactional
import kr.hhplus.be.server.common.component.ClockHolder
import kr.hhplus.be.server.common.exception.CustomException
import kr.hhplus.be.server.common.exception.ErrorCode
import kr.hhplus.be.server.common.redis.DistributedLock
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ReservationService(
	private val reservationRepository: ReservationRepository
) {
	companion object {
		private const val RESERVATION_KEY = "seatReservation:"
	}

	fun getReservationForPay(reservationId: Long, clockHolder: ClockHolder): Reservation {
		val reservation = reservationRepository.findById(reservationId)
			?: throw CustomException(ErrorCode.ENTITY_NOT_FOUND, "reservationId=$reservationId")

		if (reservation.isExpired(clockHolder.getCurrentTime())) {
			throw CustomException(ErrorCode.EXPIRED_RESERVATION)
		}

		return reservation
	}

	@DistributedLock(prefix = RESERVATION_KEY, key = "#command.concertSeatId")
	@Transactional
	fun reserve(command: ReservationCommand.Create, clockHolder: ClockHolder): Reservation {
		val seatReservation = reservationRepository.findByScheduleIdAndSeatId(command.concertScheduleId, command.concertSeatId)

		if (seatReservation != null) {
			val currentTime = clockHolder.getCurrentTime()
			require(seatReservation.isExpired(currentTime)) { throw CustomException(ErrorCode.ALREADY_RESERVED) }

			reservationRepository.delete(seatReservation)
		}

		val reservation = command.toReservation()
		return reservationRepository.save(reservation)
	}

	@Transactional
	fun makeSoldOut(reservationId: Long) {
		val reservation = reservationRepository.findById(reservationId)
			?: throw CustomException(ErrorCode.ENTITY_NOT_FOUND, "reservationId=$reservationId")

		reservation.soldOut()
		reservationRepository.save(reservation)
	}

	@Transactional
	fun rollbackReservation(reservationId: Long, expiredAt: LocalDateTime?) {
		val reservation = reservationRepository.findById(reservationId)
			?: throw CustomException(ErrorCode.ENTITY_NOT_FOUND, "reservationId=$reservationId")

		reservation.rollbackSoldOut(expiredAt)
		reservationRepository.save(reservation)
	}
}