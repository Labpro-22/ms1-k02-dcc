package com.tubes.nimons360.core.network.model

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val data: LoginData)
data class LoginData(val token: String, val expiresAt: String, val user: UserDto)

data class UserDto(val id: Int, val nim: String, val email: String, val fullName: String,
                   val createdAt: String? = null, val updatedAt: String? = null)

data class UpdateProfileRequest(val fullName: String)

data class FamilyBasic(val id: Int, val name: String, val iconUrl: String)

data class FamilyDetail(
    val id: Int, val name: String, val iconUrl: String,
    val isMember: Boolean, val familyCode: String? = null,
    val createdAt: String, val updatedAt: String,
    val members: List<MemberDto>
)
data class MemberDto(val id: Int? = null, val fullName: String, val email: String,
                     val joinedAt: String? = null)

data class FamilyWithMembers(
    val id: Int, val name: String, val iconUrl: String,
    val familyCode: String, val createdAt: String, val updatedAt: String,
    val members: List<MemberDto>
)

data class CreateFamilyRequest(val name: String, val iconUrl: String)
data class JoinFamilyRequest(val familyId: Int, val familyCode: String)
data class LeaveFamilyRequest(val familyId: Int)
data class JoinResponse(val data: JoinData)
data class JoinData(val joined: Boolean)
data class LeaveResponse(val data: LeaveData)
data class LeaveData(val left: Boolean)

// generic wrappers
data class DataWrapper<T>(val data: T)
data class ListWrapper<T>(val data: List<T>)
