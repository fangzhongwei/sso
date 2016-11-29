package com.lawsofnature.sso.service

import java.util

import Ice.ObjectImpl
import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Guice}
import com.lawsofnatrue.common.ice._
import com.lawsofnature.common.redis.{RedisClientTemplate, RedisClientTemplateImpl}
import com.lawsofnature.member.client.{MemberClientService, MemberClientServiceImpl}
import com.lawsofnature.sso.repo.{SessionRepository, SessionRepositoryImpl}


object SystemService extends App {

  private val injector = Guice.createInjector(new AbstractModule() {
    override def configure() {
      val map: util.HashMap[String, String] = ConfigHelper.configMap
      Names.bindProperties(binder(), map)
      bind(classOf[IcePrxFactory]).to(classOf[IcePrxFactoryImpl]).asEagerSingleton()
      bind(classOf[SessionRepository]).to(classOf[SessionRepositoryImpl]).asEagerSingleton()
      bind(classOf[SessionService]).to(classOf[SessionServiceImpl]).asEagerSingleton()
      bind(classOf[RedisClientTemplate]).to(classOf[RedisClientTemplateImpl]).asEagerSingleton()
      bind(classOf[MemberClientService]).to(classOf[MemberClientServiceImpl]).asEagerSingleton()
      bind(classOf[ObjectImpl]).to(classOf[SessionServiceEndpointImpl]).asEagerSingleton()
      bind(classOf[IceServerTemplate]).to(classOf[IceServerTemplateImpl]).asEagerSingleton()
    }
  })

  injector.getInstance(classOf[MemberClientService]).initClient
  injector.getInstance(classOf[RedisClientTemplate]).init
  injector.getInstance(classOf[IceServerTemplate]).startServer

}
