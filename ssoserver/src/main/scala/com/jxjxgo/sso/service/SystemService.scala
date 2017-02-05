package com.lawsofnature.sso.service

import java.util

import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Guice, TypeLiteral}
import com.jxjxgo.common.helper.ConfigHelper
import com.jxjxgo.common.redis.{RedisClientTemplate, RedisClientTemplateImpl}
import com.jxjxgo.memberber.rpc.domain.MemberEndpoint
import com.jxjxgo.scrooge.thrift.template.{ScroogeThriftServerTemplate, ScroogeThriftServerTemplateImpl}
import com.lawsofnature.sso.repo.{SessionRepository, SessionRepositoryImpl}
import com.twitter.finagle.Thrift
import com.twitter.util.Future
import com.typesafe.config.{Config, ConfigFactory}


object SystemService extends App {
  private[this] val injector = Guice.createInjector(new AbstractModule() {
    override def configure() {
      val map: util.HashMap[String, String] = ConfigHelper.configMap
      Names.bindProperties(binder(), map)

      val config: Config = ConfigFactory.load()
      bind(classOf[SessionRepository]).to(classOf[SessionRepositoryImpl]).asEagerSingleton()
      bind(classOf[SessionService]).to(classOf[SessionServiceImpl]).asEagerSingleton()
      bind(classOf[RedisClientTemplate]).to(classOf[RedisClientTemplateImpl]).asEagerSingleton()
      bind(new TypeLiteral[MemberEndpoint[Future]](){}).toInstance(Thrift.client.newIface[MemberEndpoint[Future]](config.getString("member.thrift.host.port")))
      bind(classOf[ScroogeThriftServerTemplate]).to(classOf[ScroogeThriftServerTemplateImpl]).asEagerSingleton()
    }
  })

  injector.getInstance(classOf[RedisClientTemplate]).init
  injector.getInstance(classOf[ScroogeThriftServerTemplate]).init
}
