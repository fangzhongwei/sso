package com.lawsofnature.sso.utils

/**
  * Created by fangzhongwei on 2016/10/24.
  */
object GenerateUtil extends App{
  slick.codegen.SourceCodeGenerator.main(
//    Array(slickDriver, jdbcDriver, url, outputFolder, pkg, user, password)
//      Array("slick.driver.MySQLDriver", "com.mysql.jdbc.Driver", "jdbc:mysql://localhost/member", "member", "com.lawsofnature.repo", "root", "123456")
      Array("slick.jdbc.MySQLProfile", "com.mysql.jdbc.Driver", "jdbc:mysql://localhost/sso", "sso", "com.lawsofnature.sso.repo", "root", "123456")
  )
}