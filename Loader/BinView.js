function BinView(arrayBuffer){
	
	var othis = this;
	this.arrayBuffer = arrayBuffer;
	this.dataView = new DataView(this.arrayBuffer);
	this.pos = 0;
	
	
};

BinView.prototype.readUTF8 = function (){
	var length = this.dataView.getInt16(this.pos);
	this.pos += 2;
	var view = this.arrayBuffer.slice(this.pos, this.pos + length);
	var result = new StringView(view).toString();
	this.pos += length;
	return result;
};

BinView.prototype.align4 = function() {
	// Skips to the next alignment of 4 (source should have done the same!)
	var skip = 4 - (this.pos % 4);
	if(skip > 0 && skip != 4) {
		this.pos += skip;
	};
};

BinView.prototype.readFloat = function() {
	var value = this.dataView.getFloat32(this.pos);
	this.pos += 4;
	return value;
};

BinView.prototype.readInt =function() {
	var value = this.dataView.getInt32(this.pos);
	this.pos += 4;
	return value;
};

BinView.prototype.readByte = function() {
	var value = this.dataView.getInt8(this.pos);
	this.pos += 1;
	return value;
};

BinView.prototype.readLong = function() {
	// We are throwing away the first 4 bytes here...
	var value = this.dataView.getInt32(this.pos + 4);
	this.pos += 8;
	return value;
};

BinView.prototype.readFloatArray = function(length) {

/*
	 var result = new Float32Array(this.arrayBuffer, this.pos, length);
	 this.pos += length * 4;
	 return result;
	*/

	var results = [];
	
	for (var i = 0; i < length; i++) {
		var value = this.dataView.getFloat32(this.pos);
		this.pos += 4;
		results.push(value);
	}
	
	return new Float32Array(results);

};

BinView.prototype.remaining = function(){
	return this.arrayBuffer.byteLength - this.pos;
};

