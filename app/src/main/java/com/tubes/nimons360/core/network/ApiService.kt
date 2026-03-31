interface ApiService {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("api/me")
    suspend fun getMe(): DataWrapper<UserDto>

    @PATCH("api/me")
    suspend fun updateMe(@Body request: UpdateProfileRequest): DataWrapper<UserDto>

    @GET("api/families")
    suspend fun getAllFamilies(): ListWrapper<FamilyBasic>

    @GET("api/me/families")
    suspend fun getMyFamilies(): ListWrapper<FamilyWithMembers>

    @GET("api/families/discover")
    suspend fun discoverFamilies(): ListWrapper<FamilyDetail>

    @GET("api/families/{familyId}")
    suspend fun getFamilyDetail(@Path("familyId") familyId: Int): DataWrapper<FamilyDetail>

    @POST("api/families")
    suspend fun createFamily(@Body request: CreateFamilyRequest): DataWrapper<FamilyDetail>

    @POST("api/families/join")
    suspend fun joinFamily(@Body request: JoinFamilyRequest): JoinResponse

    @POST("api/families/leave")
    suspend fun leaveFamily(@Body request: LeaveFamilyRequest): LeaveResponse
}