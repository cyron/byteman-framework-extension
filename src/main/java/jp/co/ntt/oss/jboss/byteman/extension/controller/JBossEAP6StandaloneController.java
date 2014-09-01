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

import java.util.ArrayList;
import java.util.List;

import jp.co.ntt.oss.jboss.byteman.framework.controller.AbstractNodeController;
import jp.co.ntt.oss.jboss.byteman.framework.util.DistributedConfig;

/**
 * The controller class for standalone mode of JBoss EAP6.
 *
 * The following properties are settings for the controller.
 * <table border="1">
 * <tr>
 * <th>property</th>
 * <th>required</th>
 * <th>default</th>
 * <th>description</th>
 * </tr>
 * <tr>
 * <td>node.ssh.username</td>
 * <td>true</td>
 * <td>N/A</td>
 * <td>Specify SSH user name of the target server.</td>
 * </tr>
 * <tr>
 * <td>node.ssh.password</td>
 * <td>true</td>
 * <td>N/A</td>
 * <td>Specify SSH user password of the target server.</td>
 * </tr>
 * <tr>
 * <td>node.jboss.eap6.home</td>
 * <td>true</td>
 * <td>N/A</td>
 * <td>Specify the installed directory of JBoss application server.</td>
 * </tr>
 * <tr>
 * <td>node.jboss.eap6.address</td>
 * <td>false</td>
 * <td>node.address.[identifier] value</td>
 * <td>Specify the binding address of JBoss application server.</td>
 * </tr>
 * <tr>
 * <td>node.jboss.eap6.base.dir</td>
 * <td>false</td>
 * <td>[node.jboss.eap6.home]/standalone</td>
 * <td>Specify the profile directory of JBoss application server.</td>
 * </tr>
 * <tr>
 * <td>node.jboss.eap6.profile.config</td>
 * <td>false</td>
 * <td>standalone.xml</td>
 * <td>Specify the server profile XML such as standalon.xml.</td>
 * </tr>
 * <tr>
 * <td>deployment.destination</td>
 * <td>false</td>
 * <td>N/A</td>
 * <td>Specify the directory which files will be copied to when you call
 * {@code jp.co.ntt.oss.jboss.byteman.framework.util.Deployments#deploy()}.</td>
 * </tr>
 * </table>
 * <br/>
 * And if it attaches a rule at the time of byteman agent starting, the
 * following properties can be set.
 * <table border="1">
 * <tr>
 * <th>property</th>
 * <th>description</th>
 * </tr>
 * <tr>
 * <td>node.byteman.scripts</td>
 * <td>Specify the path of rule script. In two or more cases, it divides with a
 * comma.</td>
 * </tr>
 * </table>
 */
public class JBossEAP6StandaloneController extends AbstractNodeController {

	protected List<String> moduleSystemPackages = new ArrayList<String>();

	protected String agentOptions;

	protected boolean cleanLogs = true;

	protected String logDir;

	protected String address;

	protected String jbossHome;

	protected String baseDir;

	protected String profile;

	protected String pidfile;

	/**
	 * Constructs a new instance with an identifier.
	 *
	 * @param identifier
	 *            a target node identifier
	 */
	public JBossEAP6StandaloneController(String identifier) {
		super(identifier);
		init();
	}

	/**
	 * Initializes the node infomation.
	 */
	protected void init() {
		address = getNodeConfig("node.jboss.eap6.address", getNodeConfig(DistributedConfig.NODE_ADDRESS));
		jbossHome = getNodeConfig("node.jboss.eap6.home");
		baseDir = getNodeConfig("node.jboss.eap6.base.dir", jbossHome + "/standalone");
		logDir = baseDir + "/log";
		profile = getNodeConfig("node.jboss.eap6.profile.config", "standalone.xml");
		pidfile = logDir + "/jboss.pid";
	}

	/**
	 * Sets options for the Java Agent.
	 *
	 * @param options
	 *            options for the Java Agent
	 */
	public void setAgentOptions(String agentOptions) {
		this.agentOptions = agentOptions;
	}

	/**
	 * Add the package to the -Djboss.modules.system.pkgs option.<br>
	 * In the default, this controller add following packages to the
	 * -Djboss.modules.system.pkgs option:
	 * <ul>
	 * <li>org.jboss.byteman</li>
	 * <li>jp.co.ntt.oss.jboss.byteman.framework</li>
	 * </ul>
	 *
	 * @param packageName
	 *            the package name
	 */
	public void addModuleSystemPackage(String packageName) {
		this.moduleSystemPackages.add(packageName);
	}

	/**
	 * Sets the flag which specifies whether log files are removed before
	 * starting JBoss.
	 *
	 * @param cleanLogs
	 *            If true then log files are removed. default is true.
	 */
	public void setCleanLogs(boolean cleanLogs) {
		this.cleanLogs = cleanLogs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws Exception {
		String javaOptions = null;
		String bytemanScripts = getNodeConfig("node.byteman.scripts");
		if (bytemanScripts != null) {
			javaOptions = getBytemanAgentProperties(bytemanScripts.split(","));
		} else {
			javaOptions = getBytemanAgentProperties();
		}

		if (agentOptions != null && agentOptions.length() > 0) {
			javaOptions = javaOptions + "," + agentOptions;
		}

		String options = getOptions();
		if (options.length() > 0) {
			javaOptions = javaOptions + " " + options;
		}

		javaOptions = javaOptions
				+ " -Djboss.modules.system.pkgs=org.jboss.byteman,jp.co.ntt.oss.jboss.byteman.framework";
		for (String packageName : moduleSystemPackages) {
			javaOptions = javaOptions + "," + packageName;
		}

		if (cleanLogs) {
			executeWithSSH(String.format("mkdir %s; rm -rf %s/*", logDir,
					logDir));
		}
		String exportBgflg = "export LAUNCH_JBOSS_IN_BACKGROUND=true;";
		String exportPidfile = String.format("export JBOSS_PIDFILE=%s;",
				pidfile);
		String exportBaseDir = String.format("export JBOSS_BASE_DIR=%s;", baseDir);

		String startCommand = String
				.format(exportBgflg
						+ exportPidfile
						+ exportBaseDir
						+ "export JAVA_OPTS=\"$JAVA_OPTS %s\" && bash -c \"nohup %s/bin/standalone.sh -b %s -bmanagement=%s -c %s&> %s/stdout.log &\"",
						javaOptions, jbossHome, address, address, profile,
						logDir);
		executeWithSSH(startCommand);
		logger.debug("%s is starting", identifier);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() throws Exception {
		executeWithSSH(String.format("bash -c \"kill -15 `cat %s`\"", pidfile));
		logger.debug("%s is stopping", identifier);
	}

	/**
	 * Returns the log directory path.
	 *
	 * @return the log directory.
	 */
	public String getLogDir() {
		return logDir;
	}

	/**
	 * Returns the bind address.
	 *
	 * @return the bind address.
	 */
	public String getAddress() {
		return address;
	}

}
