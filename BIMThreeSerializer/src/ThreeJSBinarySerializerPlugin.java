/**
 * 
 */
package com.sigidev.bim.threejs;

/**
 * @author Dlabz
 *
 */

import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginException;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.serializers.AbstractSerializerPlugin;
import org.bimserver.plugins.serializers.EmfSerializer;

public class ThreeJSBinarySerializerPlugin extends AbstractSerializerPlugin{

	private boolean initialized = false;
	
	
	@Override
	public String getDescription() {
		return "ThreeJSBinarySerializer";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public void init(PluginManager pluginManager) throws PluginException {
		initialized = true;
	}

	@Override
	public boolean needsGeometry() {
		return true;
	}
	
	@Override
	public EmfSerializer createSerializer(PluginConfiguration pluginConfiguration) {
		return new ThreeJSBinarySerializer();
	}

	@Override
	public String getDefaultName() {
		return "ThreeJSBinarySerializer";
	}

	@Override
	public String getDefaultContentType() {
		return "application/json";
	}

	@Override
	public String getDefaultExtension() {
		return "bin";
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public ObjectDefinition getSettingsDefinition() {
		return super.getSettingsDefinition();
	}

}
