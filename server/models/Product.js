import mongoose from 'mongoose';
const productSchema=new mongoose.Schema({sellerId:{type:mongoose.Schema.Types.ObjectId,ref:'User',required:true},name:{type:String,required:true,trim:true},description:{type:String,default:''},priceCents:{type:Number,required:true,min:1},category:{type:String,default:'General'},stock:{type:Number,required:true,min:0},image:{type:String,default:''}},{timestamps:true});
export default mongoose.model('Product',productSchema);
