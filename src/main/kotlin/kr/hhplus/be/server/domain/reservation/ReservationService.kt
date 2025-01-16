package kr.hhplus.be.server.domain.reservation

import jakarta.transaction.Transactional
import kr.hhplus.be.server.common.component.ClockHolder
import kr.hhplus.be.server.common.exception.CustomException
import kr.hhplus.be.server.common.exception.ErrorCode
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ReservationService(
	private val reservationRepository: ReservationRepository
) {

	fun getReservationForPay(reservationId: Long, clockHolder: ClockHolder): Reservation {
		val reservation = reservationRepository.findById(reservationId)
			?: throw CustomException(ErrorCode.ENTITY_NOT_FOUND, "reservationId=$reservationId")

		require(!reservation.isExpired(clockHolder.getCurrentTime())) {
			throw CustomException(ErrorCode.EXPIRED_RESERVATION)
		}

		return reservation
	}

	@Transactional
	fun reserve(command: ReservationCommand.Create, clockHolder: ClockHolder): Reservation {
		val seatReservation = reservationRepository.findByScheduleAndSeatForUpdate(command.concertScheduleId, command.concertSeatId)

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