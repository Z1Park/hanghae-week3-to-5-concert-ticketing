package kr.hhplus.be.server.domain.user

import jakarta.transaction.Transactional
import kr.hhplus.be.server.common.exception.CustomException
import kr.hhplus.be.server.common.exception.ErrorCode
import kr.hhplus.be.server.domain.user.model.PointHistory
import kr.hhplus.be.server.domain.user.model.User
import org.springframework.stereotype.Service

@Service
class UserService(
	private val userRepository: UserRepository
) {

	fun getByUuid(uuid: String): User = userRepository.findByUuid(uuid)
		?: throw CustomException(ErrorCode.ENTITY_NOT_FOUND, "userUUID=$uuid")

	@Transactional
	fun saveUserUUID(userId: Long, uuid: String): User {
		val user = userRepository.findById(userId)
			?: throw CustomException(ErrorCode.ENTITY_NOT_FOUND, "userId=$userId")

		user.updateUserUUID(uuid)
		return userRepository.save(user)
	}

	@Transactional
	fun charge(userUUID: String, chargeAmount: Int): PointHistory {
		val user = userRepository.findByUuid(userUUID)
			?: throw CustomException(ErrorCode.ENTITY_NOT_FOUND, "userUUID=$userUUID")

		val chargePointHistory = user.charge(chargeAmount)

		userRepository.save(user)
		return chargePointHistory
	}

	@Transactional
	fun use(userUUID: String, useAmount: Int): PointHistory {
		val user = userRepository.findByUuid(userUUID)
			?: throw CustomException(ErrorCode.ENTITY_NOT_FOUND, "userUUID=$userUUID")

		val usePointHistory = user.use(useAmount)

		userRepository.save(user)
		return usePointHistory
	}

	@Transactional
	fun rollbackUsePointHistory(userId: Long, pointHistoryId: Long) {
		val user = userRepository.findById(userId)
			?: throw CustomException(ErrorCode.ENTITY_NOT_FOUND, "userId=$userId")

		user.rollbackUse(pointHistoryId)
		userRepository.save(user)
	}
}