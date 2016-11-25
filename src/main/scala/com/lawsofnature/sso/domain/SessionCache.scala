package com.lawsofnature.sso.domain

/**
  * Created by fangzhongwei on 2016/11/25.
  */
class SessionCache(var token: String, var salt: String, var clientId: Int, var memberId: Long, var status: Int, var gmtCreate: Long, var lastAccessTime: Long) {
  def apply(token: String, salt: String, clientId: Int, memberId: Long, status: Int, gmtCreate: Long, lastAccessTime: Long): SessionCache = new SessionCache(token, salt, clientId, memberId, status, gmtCreate, lastAccessTime)
}
