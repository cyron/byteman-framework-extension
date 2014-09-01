/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * @authors Nippon Telegraph and Telephone Corporation
 */

package jp.co.ntt.oss.jboss.byteman.extension.controller;

import static org.mockito.Mockito.*;

import java.lang.reflect.Method;

import jp.co.ntt.oss.jboss.byteman.extension.TestUtil;
import jp.co.ntt.oss.jboss.byteman.framework.util.DistributedConfig;
import jp.co.ntt.oss.jboss.byteman.framework.util.ServerCommandManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JBossControllerTest {

	@Mock
	private ServerCommandManager commandManager;

	private DistributedConfig distributedConfig = DistributedConfig.getConfig();
	
	@Before
	public void setup() {
		try {
			MockitoAnnotations.initMocks(this);

			distributedConfig = DistributedConfig.getConfig();
			Method method = DistributedConfig.class.getDeclaredMethod("init", String.class);
			method.setAccessible(true);
			method.invoke(distributedConfig, "byteman-framework.properties");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void start_1() throws Exception {
		JBossController controller = spy(new JBossController("server2"));
		TestUtil.setValue(controller, "commandManager", commandManager);
		TestUtil.setValue(controller, "config", distributedConfig.getNodeConfig("server2"));

		controller.start();

		verify(commandManager).execute("server2",
				"mkdir /opt/jboss-5.1.2/jboss-as/server/it/log ; " +
				"export JAVA_OPTS=\"$JAVA_OPTS -javaagent:/opt/byteman.jar=sys:/opt/byteman-framework.jar," +
														 "address:127.0.2.1," +
														 "port:9091," +
														 "prop:org.jboss.byteman.jp.co.ntt.oss.jboss.byteman.framework.host=127.0.0.1," +
														 "prop:org.jboss.byteman.jp.co.ntt.oss.jboss.byteman.framework.port=1099," +
														 "script:/opt/bytemanScript1.btm," +
														 "script:/opt/bytemanScript2.btm\" ; " +
				"bash -c \"nohup /opt/jboss-5.1.2/jboss-as/bin/run.sh -c it -b 127.0.2.1 &> /opt/jboss-5.1.2/jboss-as/server/it/log/stdout.log &\"");
	}

	@Test
	public void start_2() throws Exception {
		JBossController controller = spy(new JBossController("server3"));
		TestUtil.setValue(controller, "commandManager", commandManager);
		TestUtil.setValue(controller, "config", distributedConfig.getNodeConfig("server3"));

		controller.start();

		verify(commandManager).execute("server3",
				"mkdir /opt/jboss-5.1.2/jboss-as/server/it/log ; " +
				"export JAVA_OPTS=\"$JAVA_OPTS -javaagent:/opt/byteman.jar=sys:/opt/byteman-framework.jar," +
				  "address:127.0.3.1," +
				  "port:9091," +
				  "prop:org.jboss.byteman.jp.co.ntt.oss.jboss.byteman.framework.host=127.0.0.1," +
				  "prop:org.jboss.byteman.jp.co.ntt.oss.jboss.byteman.framework.port=1099\" ; " +
				"bash -c \"nohup /opt/jboss-5.1.2/jboss-as/bin/run.sh -c it -b 127.0.3.1 &> /opt/jboss-5.1.2/jboss-as/server/it/log/stdout.log &\"");
	}

	@Test
	public void stop_1() throws Exception {
		JBossController controller = spy(new JBossController("server2"));
		TestUtil.setValue(controller, "commandManager", commandManager);
		TestUtil.setValue(controller, "config", distributedConfig.getNodeConfig("server2"));

		controller.stop();

		verify(commandManager).execute("server2",
				"bash -c \"nohup /opt/jboss-5.1.2/jboss-as/bin/shutdown.sh -S -s 127.0.2.1 -u user -p password > /dev/null 2>&1 &\"");
	}

	@Test
	public void setOptions() throws Exception {
		JBossController controller = spy(new JBossController("server2"));
		TestUtil.setValue(controller, "commandManager", commandManager);
		TestUtil.setValue(controller, "config", distributedConfig.getNodeConfig("server2"));

		controller.setOptions("-Dsun.rmi.transport.tcp.responseTimeout=30000");
		controller.start();

		verify(commandManager).execute("server2", 
				"mkdir /opt/jboss-5.1.2/jboss-as/server/it/log ; " +
				"export JAVA_OPTS=\"$JAVA_OPTS -javaagent:/opt/byteman.jar=sys:/opt/byteman-framework.jar," +
				  "address:127.0.2.1," +
				  "port:9091," +
				  "prop:org.jboss.byteman.jp.co.ntt.oss.jboss.byteman.framework.host=127.0.0.1," +
				  "prop:org.jboss.byteman.jp.co.ntt.oss.jboss.byteman.framework.port=1099," +
				  "script:/opt/bytemanScript1.btm," +
				  "script:/opt/bytemanScript2.btm " +
				  "-Dsun.rmi.transport.tcp.responseTimeout=30000\" ; " +
				"bash -c \"nohup /opt/jboss-5.1.2/jboss-as/bin/run.sh -c it -b 127.0.2.1 &> /opt/jboss-5.1.2/jboss-as/server/it/log/stdout.log &\"");
	}
}
