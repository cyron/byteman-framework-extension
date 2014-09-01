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

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;

import jp.co.ntt.oss.jboss.byteman.extension.TestUtil;
import jp.co.ntt.oss.jboss.byteman.framework.util.DistributedConfig;
import jp.co.ntt.oss.jboss.byteman.framework.util.ServerCommandManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JBossEAP6StandaloneControllerTest {

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
			method.invoke(distributedConfig, "byteman-framework-eap6.properties");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void start_1() throws Exception {
		String nodeId = "mytarget1";
		JBossEAP6StandaloneController controller = spy(new JBossEAP6StandaloneController(nodeId));
		TestUtil.setValue(controller, "commandManager", commandManager);
		TestUtil.setValue(controller, "config", distributedConfig.getNodeConfig(nodeId));
		controller.init();

		controller.start();

		verify(commandManager).execute("mytarget1",
				"mkdir /opt/jboss-eap-6.0/mytarget/log; " +
				"rm -rf /opt/jboss-eap-6.0/mytarget/log/*");
		verify(commandManager).execute("mytarget1",
				"export LAUNCH_JBOSS_IN_BACKGROUND=true;" +
				"export JBOSS_PIDFILE=/opt/jboss-eap-6.0/mytarget/log/jboss.pid;" +
				"export JBOSS_BASE_DIR=/opt/jboss-eap-6.0/mytarget;" +
				"export JAVA_OPTS=\"$JAVA_OPTS -javaagent:/opt/byteman.jar=" +
				"sys:/opt/framework-test/byteman-framework.jar," +
				"address:192.168.1.11,port:9091,prop:org.jboss.byteman.jp.co.ntt.oss.jboss.byteman.framework.host=192.168.1.1," +
				"prop:org.jboss.byteman.jp.co.ntt.oss.jboss.byteman.framework.port=1099," +
				"script:/opt/framework-test/mytarget1.btm -Djboss.modules.system.pkgs=org.jboss.byteman," +
				"jp.co.ntt.oss.jboss.byteman.framework\" && " +
				"bash -c \"nohup /opt/my-jboss-eap-6.0/bin/standalone.sh -b app1" +
				" -bmanagement=app1 -c mystandalone.xml&> /opt/jboss-eap-6.0/mytarget/log/stdout.log &\"");
	}

	@Test
	public void start_2() throws Exception {
		String nodeId = "mytarget2";
		JBossEAP6StandaloneController controller = spy(new JBossEAP6StandaloneController(nodeId));
		TestUtil.setValue(controller, "commandManager", commandManager);
		TestUtil.setValue(controller, "config", distributedConfig.getNodeConfig(nodeId));
		controller.init();

		controller.setCleanLogs(false);
		controller.start();

		verify(commandManager).execute("mytarget2",
				"export LAUNCH_JBOSS_IN_BACKGROUND=true;" +
				"export JBOSS_PIDFILE=/opt/jboss-eap-6.0/standalone/log/jboss.pid;" +
				"export JBOSS_BASE_DIR=/opt/jboss-eap-6.0/standalone;" +
				"export JAVA_OPTS=\"$JAVA_OPTS -javaagent:/opt/byteman.jar=" +
				"sys:/opt/framework-test/byteman-framework.jar," +
				"address:192.168.1.12,port:9091,prop:org.jboss.byteman.jp.co.ntt.oss.jboss.byteman.framework.host=192.168.1.1," +
				"prop:org.jboss.byteman.jp.co.ntt.oss.jboss.byteman.framework.port=1099," +
				"script:/opt/framework-test/mytarget2.btm -Djboss.modules.system.pkgs=org.jboss.byteman," +
				"jp.co.ntt.oss.jboss.byteman.framework\" && " +
				"bash -c \"nohup /opt/jboss-eap-6.0/bin/standalone.sh -b 192.168.1.12" +
				" -bmanagement=192.168.1.12 -c standalone-full.xml&> /opt/jboss-eap-6.0/standalone/log/stdout.log &\"");
	}

	@Test
	public void stop_1() throws Exception {
		String nodeId = "mytarget1";
		JBossEAP6StandaloneController controller = spy(new JBossEAP6StandaloneController(nodeId));
		TestUtil.setValue(controller, "commandManager", commandManager);
		TestUtil.setValue(controller, "config", distributedConfig.getNodeConfig(nodeId));
		controller.init();

		controller.stop();

		verify(commandManager).execute("mytarget1",
				"bash -c \"kill -15 `cat /opt/jboss-eap-6.0/mytarget/log/jboss.pid`\"");
	}

	@Test
	public void setOptions() throws Exception {
		String nodeId = "mytarget1";
		JBossEAP6StandaloneController controller = spy(new JBossEAP6StandaloneController(nodeId));
		TestUtil.setValue(controller, "commandManager", commandManager);
		TestUtil.setValue(controller, "config", distributedConfig.getNodeConfig(nodeId));
		controller.init();

		controller.setCleanLogs(false);
		controller.setOptions("-Dsun.rmi.transport.tcp.responseTimeout=30000");
		controller.start();

		verify(commandManager).execute("mytarget1",
				"export LAUNCH_JBOSS_IN_BACKGROUND=true;" +
				"export JBOSS_PIDFILE=/opt/jboss-eap-6.0/mytarget/log/jboss.pid;" +
				"export JBOSS_BASE_DIR=/opt/jboss-eap-6.0/mytarget;" +
				"export JAVA_OPTS=\"$JAVA_OPTS -javaagent:/opt/byteman.jar=" +
				"sys:/opt/framework-test/byteman-framework.jar," +
				"address:192.168.1.11,port:9091,prop:org.jboss.byteman.jp.co.ntt.oss.jboss.byteman.framework.host=192.168.1.1," +
				"prop:org.jboss.byteman.jp.co.ntt.oss.jboss.byteman.framework.port=1099," +
				"script:/opt/framework-test/mytarget1.btm -Dsun.rmi.transport.tcp.responseTimeout=30000 -Djboss.modules.system.pkgs=org.jboss.byteman," +
				"jp.co.ntt.oss.jboss.byteman.framework\" && " +
				"bash -c \"nohup /opt/my-jboss-eap-6.0/bin/standalone.sh -b app1" +
				" -bmanagement=app1 -c mystandalone.xml&> /opt/jboss-eap-6.0/mytarget/log/stdout.log &\"");
	}

	@Test
	public void setAgentOptions() throws Exception {
		String nodeId = "mytarget1";
		JBossEAP6StandaloneController controller = spy(new JBossEAP6StandaloneController(nodeId));
		TestUtil.setValue(controller, "commandManager", commandManager);
		TestUtil.setValue(controller, "config", distributedConfig.getNodeConfig(nodeId));
		controller.init();

		controller.setCleanLogs(false);
		controller.setAgentOptions("agetnOpt:test");
		controller.start();

		verify(commandManager).execute("mytarget1",
				"export LAUNCH_JBOSS_IN_BACKGROUND=true;" +
				"export JBOSS_PIDFILE=/opt/jboss-eap-6.0/mytarget/log/jboss.pid;" +
				"export JBOSS_BASE_DIR=/opt/jboss-eap-6.0/mytarget;" +
				"export JAVA_OPTS=\"$JAVA_OPTS -javaagent:/opt/byteman.jar=" +
				"sys:/opt/framework-test/byteman-framework.jar," +
				"address:192.168.1.11,port:9091,prop:org.jboss.byteman.jp.co.ntt.oss.jboss.byteman.framework.host=192.168.1.1," +
				"prop:org.jboss.byteman.jp.co.ntt.oss.jboss.byteman.framework.port=1099," +
				"script:/opt/framework-test/mytarget1.btm,agetnOpt:test -Djboss.modules.system.pkgs=org.jboss.byteman," +
				"jp.co.ntt.oss.jboss.byteman.framework\" && " +
				"bash -c \"nohup /opt/my-jboss-eap-6.0/bin/standalone.sh -b app1" +
				" -bmanagement=app1 -c mystandalone.xml&> /opt/jboss-eap-6.0/mytarget/log/stdout.log &\"");
	}

	@Test
	public void addModuleSystemPackage() throws Exception {
		String nodeId = "mytarget1";
		JBossEAP6StandaloneController controller = spy(new JBossEAP6StandaloneController(nodeId));
		TestUtil.setValue(controller, "commandManager", commandManager);
		TestUtil.setValue(controller, "config", distributedConfig.getNodeConfig(nodeId));
		controller.init();

		controller.setCleanLogs(false);
		controller.addModuleSystemPackage("my.test.package");
		controller.start();

		verify(commandManager).execute("mytarget1",
				"export LAUNCH_JBOSS_IN_BACKGROUND=true;" +
				"export JBOSS_PIDFILE=/opt/jboss-eap-6.0/mytarget/log/jboss.pid;" +
				"export JBOSS_BASE_DIR=/opt/jboss-eap-6.0/mytarget;" +
				"export JAVA_OPTS=\"$JAVA_OPTS -javaagent:/opt/byteman.jar=" +
				"sys:/opt/framework-test/byteman-framework.jar," +
				"address:192.168.1.11,port:9091,prop:org.jboss.byteman.jp.co.ntt.oss.jboss.byteman.framework.host=192.168.1.1," +
				"prop:org.jboss.byteman.jp.co.ntt.oss.jboss.byteman.framework.port=1099," +
				"script:/opt/framework-test/mytarget1.btm -Djboss.modules.system.pkgs=org.jboss.byteman," +
				"jp.co.ntt.oss.jboss.byteman.framework,my.test.package\" && " +
				"bash -c \"nohup /opt/my-jboss-eap-6.0/bin/standalone.sh -b app1" +
				" -bmanagement=app1 -c mystandalone.xml&> /opt/jboss-eap-6.0/mytarget/log/stdout.log &\"");
	}
}
