package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PageDao {
    @Query("SELECT * FROM creator_pages ORDER BY id DESC")
    fun getAllPagesFlow(): Flow<List<Page>>

    @Query("SELECT * FROM creator_pages WHERE id = :id LIMIT 1")
    suspend fun getPageById(id: Int): Page?

    @Query("SELECT COUNT(*) FROM creator_pages")
    suspend fun getPageCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: Page): Long

    @Update
    suspend fun updatePage(page: Page)

    @Query("DELETE FROM creator_pages")
    suspend fun deleteAllPages()
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPostsFlow(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE pageId = :pageId ORDER BY timestamp DESC")
    fun getPostsByPageFlow(pageId: Int): Flow<List<Post>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post): Long

    @Update
    suspend fun updatePost(post: Post)

    @Delete
    suspend fun deletePost(post: Post)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deletePostById(id: Int)

    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts()
}

@Dao
interface CampaignDao {
    @Query("SELECT * FROM campaigns ORDER BY id DESC")
    fun getAllCampaignsFlow(): Flow<List<Campaign>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign): Long

    @Update
    suspend fun updateCampaign(campaign: Campaign)

    @Query("DELETE FROM campaigns WHERE id = :id")
    suspend fun deleteCampaignById(id: Int)
}
