package com.jxjxgo.sso.test

import com.jxjxgo.sso.rpc.domain.SSOServiceEndpoint
import com.twitter.finagle.Thrift
import com.twitter.util.{Await, Future}
import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by fangzhongwei on 2017/2/5.
  */
object SSOTest {

  def main(args: Array[String]): Unit = {
    val config: Config = ConfigFactory.load()
    val endpoint: SSOServiceEndpoint[Future] = Thrift.client.newIface[SSOServiceEndpoint[Future]](config.getString("finagle.thrift.host.port"))
    println(Await.result(endpoint.touch("aaa", "abc")))
  }

}
