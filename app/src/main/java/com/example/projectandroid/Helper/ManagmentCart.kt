package com.example.projectandroid.Helper

import android.content.Context
import android.widget.Toast
import com.example.projectandroid.Domain.FoodModel

class ManagmentCart(val context: Context) {

    private val tinyDB = TinyDB(context)

    fun insertItem(item: FoodModel) {
        var listFood = getListCart()
        val existAlready = listFood.any { it.Title == item.Title }
        val index = listFood.indexOfFirst { it.Title == item.Title }

        if (existAlready) {
            listFood[index].numberInCart = item.numberInCart
        } else {
            listFood.add(item)
        }
        tinyDB.putListObject("CartList", listFood)
        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show()
    }

    fun getListCart(): ArrayList<FoodModel> {
        return tinyDB.getListObject("CartList") ?: arrayListOf()
    }

    fun minusItem(listFood: ArrayList<FoodModel>, position: Int, listener: ChangeNumberItemsListener) {
        if (position < 0 || position >= listFood.size) return
        val currentCount = listFood[position].numberInCart
        if (currentCount <= 1) {
            listFood.removeAt(position)
        } else {
            listFood[position].numberInCart = currentCount - 1
        }
        tinyDB.putListObject("CartList", listFood)
        listener.onChanged()
    }

    fun plusItem(listFood: ArrayList<FoodModel>, position: Int, listener: ChangeNumberItemsListener) {
        listFood[position].numberInCart++
        tinyDB.putListObject("CartList", listFood)
        listener.onChanged()
    }

    fun getTotalFee(): Double {
        val listFood = getListCart()
        var fee = 0.0
        for (item in listFood) {
            fee += item.Price * item.numberInCart
        }
        return fee
    }

    // ===== CÁC METHOD MỚI CHO ORDER FLOW =====

    /**
     * Xóa tất cả sản phẩm trong cart
     */
    fun clearCart() {
        tinyDB.remove("CartList")
        Toast.makeText(context, "Cart cleared", Toast.LENGTH_SHORT).show()
    }

    /**
     * Lưu đơn hàng đã thanh toán vào SharedPreferences
     * @param paidItems danh sách sản phẩm đã thanh toán
     */
    fun savePaidOrder(paidItems: ArrayList<FoodModel>) {
        // Tạo một copy của list để tránh reference issues
        val orderItems = ArrayList<FoodModel>()
        for (item in paidItems) {
            // Tạo một copy của từng item
            val orderItem = FoodModel().apply {
                this.Title = item.Title
                this.Price = item.Price
                this.numberInCart = item.numberInCart
                this.ImagePath = item.ImagePath
                this.Description = item.Description
                this.CategoryId = item.CategoryId
                this.LocationId = item.LocationId
                this.TimeId = item.TimeId
                this.PriceId = item.PriceId
                this.Star = item.Star
            }
            orderItems.add(orderItem)
        }

        tinyDB.putListObject("PaidOrder", orderItems)
    }

    /**
     * Lấy danh sách đơn hàng đã thanh toán
     * @return ArrayList<FoodModel> danh sách sản phẩm đã thanh toán
     */
    fun getPaidOrder(): ArrayList<FoodModel> {
        return tinyDB.getListObject("PaidOrder") ?: arrayListOf()
    }

    /**
     * Xóa đơn hàng đã thanh toán (nếu cần)
     */
    fun clearPaidOrder() {
        tinyDB.remove("PaidOrder")
    }

    /**
     * Kiểm tra xem có đơn hàng đã thanh toán hay không
     * @return true nếu có đơn hàng, false nếu không
     */
    fun hasPaidOrder(): Boolean {
        val paidOrder = getPaidOrder()
        return paidOrder.isNotEmpty()
    }

    /**
     * Lấy tổng số tiền của đơn hàng đã thanh toán
     * @return Double tổng số tiền
     */
    fun getPaidOrderTotal(): Double {
        val paidOrder = getPaidOrder()
        var total = 0.0
        for (item in paidOrder) {
            total += item.Price * item.numberInCart
        }
        return total
    }

    /**
     * Lấy tổng số lượng items trong đơn hàng đã thanh toán
     * @return Int tổng số lượng
     */
    fun getPaidOrderItemCount(): Int {
        val paidOrder = getPaidOrder()
        var count = 0
        for (item in paidOrder) {
            count += item.numberInCart
        }
        return count
    }
}