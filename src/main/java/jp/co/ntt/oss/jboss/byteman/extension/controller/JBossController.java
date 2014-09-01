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

import jp.co.ntt.oss.jboss.byteman.framework.controller.AbstractNodeController;
import jp.co.ntt.oss.jboss.byteman.framework.util.DistributedConfig;

/**
 * The controller class for JBoss EAP5.1.
 *
 * In addition to a basic setup, the following properties are required.
 * <table border="1">
 * <tr><th>property</th><th>description</th></tr>
 * <tr><td>node.jboss.home</td><td>Sets the installed directory of JBoss EAP application server.</td></tr>
 * <tr><td>node.jboss.server</td><td>Sets the server setting directory for JBoss EAP.</td></tr>
 * <tr><td>node.jboss.jmxuser</td><td>Sets the JMX user name.</td></tr>
 * <tr><td>node.jboss.jmxpassword</td><td>Sets the JMX password.</td></tr>
 * </table>
 * <br/>
 * And if it attaches a rule at the time of byteman agent starting, the following properties can be set.
 * <table border="1">
 * <tr><th>property</th><th>description</th></tr>
 * <tr><td>node.byteman.scripts</td><td>Sets the path of rule script. In two or more cases, it divides with a comma.</td></tr>
 * </table>
 */
public class JBossController extends AbstractNodeController {

	/**
	 * Constructs a new instance with the identifier.
	 *
	 * @param identifier the identifier
	 */
	public JBossController(String identifier) {
		super(identifier);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws Exception {
		String address = getAddress();
		String jbossHome = getNodeConfig("node.jboss.home");
		String serverDir = getNodeConfig("node.jboss.server");
		String javaOptions = null;
		String bytemanScripts = getNodeConfig("node.byteman.scripts");
		if(bytemanScripts != null) {
			javaOptions = getBytemanAgentProperties(bytemanScripts.split(","));
		} else {
			javaOptions = getBytemanAgentProperties();
		}
		String options = getOptions();
		if(options.length() > 0){
			javaOptions = javaOptions + " " + options;
		}
		executeWithSSH(String.format(
				"mkdir %s/server/%s/log ; " +
				"export JAVA_OPTS=\"$JAVA_OPTS %s\" ; " +
				"bash -c \"nohup %s/bin/run.sh -c %s -b %s &> %s/server/%s/log/stdout.log &\"",
				jbossHome, serverDir,
				javaOptions,
				jbossHome, serverDir, address, jbossHome, serverDir));
		logger.debug("%s is starting", identifier);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() throws Exception {
		String address = getAddress();
		String jbossHome = getNodeConfig("node.jboss.home");
		String jmxUser = getNodeConfig("node.jboss.jmxuser");
		String jmxPassword = getNodeConfig("node.jboss.jmxpassword");
		executeWithSSH(String.format(
				"bash -c \"nohup %s/bin/shutdown.sh -S -s %s -u %s -p %s > /dev/null 2>&1 &\"",
				jbossHome, address, jmxUser, jmxPassword));
		logger.debug("%s is stopping", identifier);
	}

	private String getAddress() {
		String address = getNodeConfig("node.jboss.address");
		if(address != null) {
			return address;
		} else {
			return getNodeConfig(DistributedConfig.NODE_ADDRESS);
		}
	}
}
