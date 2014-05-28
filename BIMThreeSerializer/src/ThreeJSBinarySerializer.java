package com.sigidev.bim.threejs;
//TODO: continue this

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.models.ifc2x3tc1.GeometryData;
import org.bimserver.models.ifc2x3tc1.GeometryInfo;
import org.bimserver.models.ifc2x3tc1.GeometryInstance;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.renderengine.RenderEnginePlugin;
import org.bimserver.plugins.serializers.AbstractGeometrySerializer;
import org.bimserver.plugins.serializers.ProjectInfo;
import org.bimserver.plugins.serializers.SerializerException;

import com.sigidev.bim.threejs.Bounds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.System;
//Serializes geometry instances 
public class ThreeJSBinarySerializer extends AbstractGeometrySerializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ThreeJSBinarySerializer.class);
	private static final byte FORMAT_VERSION = 4;
	private final HashMap<Long,GeometryData> geometriesMap = new HashMap<Long,GeometryData>();
	private final HashMap<Long,IfcProduct> productsMap = new HashMap<Long,IfcProduct>();
	private final HashMap<Long,Long> productGeometryMap = new HashMap<Long,Long>();
	@Override
	public void init(IfcModelInterface model, ProjectInfo projectInfo, PluginManager pluginManager, RenderEnginePlugin renderEnginePlugin, boolean normalizeOids) throws SerializerException {
		super.init(model, projectInfo, pluginManager, renderEnginePlugin, normalizeOids);
		
	}

	@Override
	public void reset() {
		geometriesMap.clear();
		productsMap.clear();
		productGeometryMap.clear();
		setMode(Mode.BODY);
	}

	@Override
	public boolean write(OutputStream out) throws SerializerException {
		if (getMode() == Mode.BODY) {
			
			DataOutputStream dataOutputStream = new DataOutputStream(out);

			try {
				//calculateGeometryExtents();
				writeGeometries(dataOutputStream);
			} catch (Exception e) {
				LOGGER.error("", e);
			}
			setMode(Mode.FINISHED);
			return true;
		} else if (getMode() == Mode.FINISHED) {
			return false;
		}
		return false;
	}


	void writeGeometries(DataOutputStream dataOutputStream) throws IOException {
		
		
		dataOutputStream.writeUTF("3JS");
		dataOutputStream.writeByte(FORMAT_VERSION);
		dataOutputStream.flush();

		List<IfcProduct> products = getModel().getAllWithSubTypes(IfcProduct.class);
		for (IfcProduct ifcProduct : products) {
			GeometryInfo geometryInfo = ifcProduct.getGeometry();
			if (geometryInfo != null && geometryInfo.getTransformation() != null && geometryInfo.getData().getVertices() != null) {
				System.out.println("Mapping " +ifcProduct.getOid() + " : " + ifcProduct.toString());
				productsMap.put(ifcProduct.getOid(), ifcProduct);
				GeometryData geometryData = geometryInfo.getData();
								
				productGeometryMap.put(ifcProduct.getOid(), geometryData.getOid());
				
				if (!geometriesMap.containsKey(geometryData.getOid())) {
					geometriesMap.put(geometryData.getOid(), geometryData);
					System.out.println("new");
					
				}
			}
		}
		dataOutputStream.writeInt(geometriesMap.size());
		dataOutputStream.flush();
		
		int counter = 0;
		for (GeometryData geometryData : geometriesMap.values()){
			System.out.println("Sending geometry "+geometryData.getOid()+" ...");
			
			
			// BEWARE, ByteOrder is always LITTLE_ENDIAN, because that's what GPU's seem to prefer, Java's ByteBuffer default is BIG_ENDIAN though!				
			
			ByteBuffer vertexByteBuffer = ByteBuffer.wrap(geometryData.getVertices());
			//vertexByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			ByteBuffer indicesBuffer = ByteBuffer.wrap(geometryData.getIndices());
			//indicesBuffer.order(ByteOrder.LITTLE_ENDIAN);
			ByteBuffer normalsBuffer = ByteBuffer.wrap(geometryData.getNormals());
			//normalsBuffer.order(ByteOrder.LITTLE_ENDIAN);
			int nrV = vertexByteBuffer.capacity();
			int nrI = indicesBuffer.capacity();
			int nrN = normalsBuffer.capacity();
			int nrB = nrV + nrI + nrN + 8;
			System.out.println( nrV +"-"+nrI + "-"+nrN);
			dataOutputStream.writeUTF("BGM");
			dataOutputStream.writeInt(nrB);
			
			dataOutputStream.writeLong(geometryData.getOid());
			
			dataOutputStream.writeInt(vertexByteBuffer.capacity() / 4);
			dataOutputStream.write(vertexByteBuffer.array());

			dataOutputStream.writeInt(indicesBuffer.capacity() / 4);
			dataOutputStream.write(indicesBuffer.array());
			
			dataOutputStream.writeInt(normalsBuffer.capacity() / 4);
			dataOutputStream.write(normalsBuffer.array());
			
			counter++;
			if (counter % 12 == 0) {
				dataOutputStream.flush();
			}
	
		}
		//dataOutputStream.flush();
		for (IfcProduct ifcProduct : productsMap.values()){
			Long poid = ifcProduct.getOid();
			String type = ifcProduct.eClass().getName();
			System.out.println("Sending product "+poid+" ..." + type );
			
			Long geometryId = productGeometryMap.get(ifcProduct.getOid());
			if(geometryId != null){
				System.out.println("  goid: "+geometryId);
			}else{
				System.out.println("error!!!");
			}
			GeometryInfo geometryInfo = ifcProduct.getGeometry();
			
			//GeometryData geometryData = geometriesMap.get(geometryId);
			//DEBUG START
			byte[] t = geometryInfo.getTransformation();
			
			ByteBuffer tb = ByteBuffer.wrap(t);
			tb.order(ByteOrder.LITTLE_ENDIAN);
			tb.position(0);
			FloatBuffer tf = tb.asFloatBuffer();
			
			tf.position(0);
			System.out.println("--MATRIX--");
			System.out.println(tf.get(0)+", "+tf.get(4)+", "+tf.get(8)+", "+tf.get(12));
			System.out.println(tf.get(1)+", "+tf.get(5)+", "+tf.get(9)+", "+tf.get(13));
			System.out.println(tf.get(2)+", "+tf.get(6)+", "+tf.get(10)+", "+tf.get(14));
			System.out.println(tf.get(3)+", "+tf.get(7)+", "+tf.get(11)+", "+tf.get(15));
			//DEBUG END
		
			ByteBuffer transformationBuffer = ByteBuffer.wrap(geometryInfo.getTransformation());
			
			int nrM = transformationBuffer.capacity();
			System.out.println("goid:" + geometryId + " : " + nrM);
			
			
			dataOutputStream.writeUTF("MSH");
			dataOutputStream.writeLong(poid);
			dataOutputStream.writeUTF(type);
			dataOutputStream.writeLong(geometryId);

			dataOutputStream.write(transformationBuffer.array());
			
			counter++;
			if (counter % 12 == 0) {
				dataOutputStream.flush();
			
			}
		}
		dataOutputStream.writeUTF("END");
		dataOutputStream.flush();
	}
	

}
