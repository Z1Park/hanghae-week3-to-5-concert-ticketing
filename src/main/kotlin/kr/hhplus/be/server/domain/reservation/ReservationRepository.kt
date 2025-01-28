package kr.hhplus.be.server.domain.reservation

import kr.hhplus.be.server.domain.reservation.model.Reservation

interface ReservationRepository {

	fun findById(reservationId: Long): Reservation?

	fun findByScheduleIdAndSeatId(concertScheduleId: Long, concertSeatId: Long): Reservation?

	fun save(reservation: Reservation): Reservation

	fun delete(seatReservation: Reservation)
}