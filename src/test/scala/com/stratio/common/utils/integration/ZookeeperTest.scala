/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.common.utils.integration

import com.stratio.common.utils.components.config.impl.TypesafeConfigComponent
import com.stratio.common.utils.components.dao.GenericDAOComponent
import com.stratio.common.utils.components.logger.impl.Slf4jLoggerComponent
import com.stratio.common.utils.components.repository.impl.ZookeeperRepositoryComponent
import org.apache.curator.test.TestingServer
import org.apache.curator.utils.CloseableUtils
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class ZookeeperIntegrationTest extends WordSpec
  with Matchers
  with BeforeAndAfterAll {

  val ZookeeperTestPort: Int = 10666

  var zkTestServer: TestingServer = _

  def zookeeperStart: Unit = {
    zkTestServer = new TestingServer(ZookeeperTestPort)
    zkTestServer.start()
  }

  def zookeeperStop: Unit = {
    CloseableUtils.closeQuietly(zkTestServer)
    zkTestServer.stop()
  }

  override def beforeAll {
    zookeeperStart
  }

  override def afterAll {
    zookeeperStop
  }

  "A dao component" should {

    val component = new DummyZookeeperDAOComponent
    import component.dao

    "save a dummy in ZK and get it" in {
      dao.create("test1", Dummy("value"))
      dao.get("test1") should be(Success(Some(Dummy("value"))))
    }

    "update the dummy in ZK and get it" in {
      dao.update("test1", Dummy("newValue"))
      dao.get("test1") should be(Success(Some(Dummy("newValue"))))
    }

    "upser the dummy in ZK and get it" in {
      dao.upsert("test1", Dummy("newValue"))
      dao.get("test1") should be(Success(Some(Dummy("newValue"))))
      dao.upsert("test1", Dummy("newValue2"))
      dao.get("test1") should be(Success(Some(Dummy("newValue2"))))
    }

    "delete the dummy in ZK and get it with a None result" in {
      dao.delete("test1")
      dao.exists("test1") should be (Success(false))
    }

    "save a dummy in ZK and delete all" in {
      dao.create("test1", Dummy("value"))
      dao.get("test1") should be(Success(Some(Dummy("value"))))
      dao.deleteAll
      dao.exists("test1") should be (Success(false))
      dao.count() shouldBe a[Failure[_]]
    }
  }
}

class DummyZookeeperDAOComponent extends GenericDAOComponent[Dummy]
  with ZookeeperRepositoryComponent
  with TypesafeConfigComponent
  with Slf4jLoggerComponent {

  override val dao : DAO = new GenericDAO(Option("dummy"))

}

case class Dummy(property: String) {}