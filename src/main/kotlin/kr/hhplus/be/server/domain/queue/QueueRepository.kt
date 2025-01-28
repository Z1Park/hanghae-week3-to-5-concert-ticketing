package kr.hhplus.be.server.domain.queue

import kr.hhplus.be.server.domain.queue.model.Queue
import kr.hhplus.be.server.domain.queue.model.QueueActiveStatus
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface QueueRepository {

	fun findById(tokenId: Long): Queue?

	fun findByUUID(tokenUUID: String): Queue?

	fun findAllByActivateStatusAndExpiredAtBefore(activateStatus: QueueActiveStatus, expiredAt: LocalDateTime): List<Queue>

	fun countByActivateStatusAndExpiredAtAfter(activateStatus: QueueActiveStatus, expiredAt: LocalDateTime): Int

	fun findAllOrderByCreatedAt(activateStatus: QueueActiveStatus, pageable: Pageable): List<Queue>

	fun findAllOrderByCreatedAtDesc(activateStatus: QueueActiveStatus, pageable: Pageable): List<Queue>

	fun save(queue: Queue): Queue

	fun saveAll(queues: List<Queue>)
}