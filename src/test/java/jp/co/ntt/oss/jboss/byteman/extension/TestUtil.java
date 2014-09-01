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

package jp.co.ntt.oss.jboss.byteman.extension;

import java.lang.reflect.Field;

public class TestUtil {

	public static Object getValue(Object target, String fieldName) throws Exception {
		Class<?> clazz = target.getClass();

		// gets a raw class if target is a mock object
		if(clazz.getName().indexOf("$$EnhancerByMockito") >= 0){
			clazz = clazz.getSuperclass();
		}

		Field field = null;
		try {
			field = clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			field = clazz.getSuperclass().getDeclaredField(fieldName);
		}
		field.setAccessible(true);
		return field.get(target);
	}

	public static <T> void setValue(Object target, String fieldName, T value) throws Exception {
		Class<?> clazz = target.getClass();

		// gets a raw class if target is a mock object
		if(clazz.getName().indexOf("$$EnhancerByMockito") >= 0){
			clazz = clazz.getSuperclass();
		}

		Field field = null;
		try {
			field = clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			field = clazz.getSuperclass().getDeclaredField(fieldName);
		}
		field.setAccessible(true);
		field.set(target, value);
	}
}
