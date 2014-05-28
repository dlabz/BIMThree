function ThreeView(binView){
	this.bin = binView;
	this.head = this.bin.readUTF8();
	this.version = this.bin.readByte();
	this.length = this.bin.readInt();
	this.remaining = this.length;
	console.log("BIN HEAD:", this.bin.pos);
}

ThreeView.prototype.readBGM = function(){
	//total bytes
	var NEED = this.bin.readInt();
	var geom = {type:"BufferGeometry"};
	geom.oid = this.bin.readLong();
	geom.attributes = {};
	
	var nrV = this.bin.readInt();
	geom.attributes.position = {
		itemSize:3,
		type: "Float32Array",
		array: this.bin.readFloatArray(nrV)
	};
	
	var nrI = this.bin.readInt();
	geom.attributes.index = {
		itemSize:3,
		type: "Float32Array",
		array: this.bin.readFloatArray(nrI)
	};	
	
	var nrN = this.bin.readInt();
	geom.attributes.normal = {
		itemSize:3,
		type: "Float32Array",
		array: this.bin.readFloatArray(nrN)
	};
	//console.log(geom);
	return geom;
};

ThreeView.prototype.readMSH = function(){

	var mesh = {
		type : "Mesh",
		poid : this.bin.readLong(),
		material : this.bin.readUTF8(),
		geometry : this.bin.readLong(),
		matrix : this.bin.readFloatArray(16)
	};
	//console.log(mesh);
	return mesh;
};

ThreeView.prototype.read = function(){
	if (this.remaining <= 0) return false;
	var TYPE = this.bin.readUTF8();
	console.log(TYPE);
	
	switch(TYPE){
		case "BGM":
		//read binary geometry
		return this.readBGM();
		break;
		
		case "MSH":
		//read Mesh
		return this.readMSH();
		break;
		
		case "END":
		default:
		//false
		return false;
	}
	
};
