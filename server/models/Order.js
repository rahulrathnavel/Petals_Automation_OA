import mongoose from 'mongoose';
const orderSchema=new mongoose.Schema({buyerId:{type:mongoose.Schema.Types.ObjectId,ref:'User',required:true},items:[{productId:{type:mongoose.Schema.Types.ObjectId,ref:'Product'},sellerId:{type:mongoose.Schema.Types.ObjectId,ref:'User'},name:String,priceCents:Number,quantity:Number}],totalCents:{type:Number,required:true},status:{type:String,default:'completed'}},{timestamps:true});
export default mongoose.model('Order',orderSchema);
