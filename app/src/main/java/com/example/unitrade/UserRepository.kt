package com.example.unitrade

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object UserRepository {

    interface UserCallback {
        fun onSuccess(user: User)
        fun onFailure(e: Exception)
    }

    interface ProductsCallback {
        fun onSuccess(products: List<Product>)
        fun onFailure(e: Exception)
    }

    interface UpdateCallback {
        fun onSuccess()
        fun onFailure(e: Exception)
    }

    fun getUserByUid(uid: String, callback: UserCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val users = SupabaseClient.client.postgrest["users"]
                    .select {
                        eq("id", uid)
                    }
                    .decodeList<User>()
                if (users.isNotEmpty()) {
                    val user = users[0]
                    withContext(Dispatchers.Main) {
                        callback.onSuccess(user)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callback.onFailure(Exception("User not found"))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }

    fun getAllProducts(callback: ProductsCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val products = SupabaseClient.client.postgrest["products"]
                    .select()
                    .decodeList<Product>()
                withContext(Dispatchers.Main) {
                    callback.onSuccess(products)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }

    fun updateUser(user: User, callback: UpdateCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SupabaseClient.client.postgrest["users"]
                    .update({
                        set("full_name", user.fullName)
                        set("username", user.username)
                        set("email", user.email)
                        set("phone_number", user.phoneNumber)
                        set("bio", user.bio)
                        set("profile_image_url", user.profileImageUrl)
                        set("profile_image_version", user.profileImageVersion)
                        set("address", user.address)
                        set("last_edited", user.lastEdited)
                    }) {
                        eq("id", user.id)
                    }
                withContext(Dispatchers.Main) {
                    callback.onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }
}