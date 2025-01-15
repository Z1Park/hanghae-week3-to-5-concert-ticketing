package kr.hhplus.be.server.domain.concert

import kr.hhplus.be.server.common.exception.CustomException
import kr.hhplus.be.server.common.exception.ErrorCode
import org.springframework.stereotype.Service

@Service
class ConcertService(
	private val concertRepository: ConcertRepository
) {

	fun getConcert(): List<ConcertInfo.ConcertDto> {
		val concerts = concertRepository.findAllConcert(false)

		return concerts.map { ConcertInfo.ConcertDto.from(it) }
	}

	fun getConcertSchedule(concertId: Long): List<ConcertInfo.Schedule> {
		val concert = concertRepository.findConcert(concertId)
			?: throw CustomException(ErrorCode.ENTITY_NOT_FOUND, "concertId=$concertId")

		val concertSchedules = concertRepository.findAllScheduleByConcertId(concert.id)
		return concertSchedules.map { ConcertInfo.Schedule.from(it) }
	}

	fun getConcertSeat(concertId: Long, concertScheduleId: Long): List<ConcertInfo.Seat> {
		val schedule = concertRepository.findSchedule(concertScheduleId)
			?: throw CustomException(ErrorCode.ENTITY_NOT_FOUND, "concertScheduleId=$concertScheduleId")

		val concertSeats = concertRepository.findAllSeatByConcertScheduleId(schedule.id)
		return concertSeats.map { ConcertInfo.Seat.of(concertId, it) }
	}

	fun getConcertSeatDetailInformation(command: ConcertCommand.Total): ConcertInfo.Detail {
		val concert = concertRepository.findConcert(command.concertId)
			?: throw CustomException(ErrorCode.ENTITY_NOT_FOUND, "concertId=${command.concertId}")
		val concertSchedule = concertRepository.findSchedule(command.concertScheduleId)
			?: throw CustomException(ErrorCode.ENTITY_NOT_FOUND, "concertScheduleId=${command.concertScheduleId}")
		val concertSeat = concertRepository.findSeat(command.concertSeatId)
			?: throw CustomException(ErrorCode.ENTITY_NOT_FOUND, "concertSeatId=${command.concertSeatId}")

		require(concertSchedule.isOnConcert(concert.id)) {
			throw CustomException(ErrorCode.NOT_MATCH_SCHEDULE, "concertId=${concert.id}, concertScheduleId=${concertSchedule.id}")
		}
		require(concertSeat.isOnConcertSchedule(concertSchedule.id)) {
			throw CustomException(ErrorCode.NOT_MATCH_SEAT, "concertScheduleId=${concertSchedule.id}, concertSeatId=${concertSeat.id}")
		}

		return ConcertInfo.Detail.of(concert, concertSchedule, concertSeat)
	}
}