package kr.co.lina.ga.photogallery

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kr.co.lina.ga.R
import kr.co.lina.ga.photogallery.model.Item
import kr.co.lina.ga.utils.WaLog

/**
 * 이미지 업로드 미리보기 어댑터
 * layout : recycler_images_item.xml
 */
class ImagesAdapter(val context: Context, var clickListener: ImagesItemClickListener) :
    RecyclerView.Adapter<ImagesAdapter.ImageViewHolder>() {

    private val tag = "ImagesAdapter"
    private val Log = WaLog
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    var items = arrayListOf<Item>()
    //val mGalleryActivity = context as GalleryActivity

    /**
     * 이미지를 출력할 뷰 준비
     * @param parent
     * @param viewType
     * @return ImageViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(inflater.inflate(R.layout.recycler_images_item, parent, false))
    }

    /**
     * 이미지 갯수 가져오기
     * @return Int
     */
    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * 뷰홀더에 이미지 셋팅
     * @param holder
     * @param position
     */
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val item = items[position]
        Log.i(tag, "onBindViewHolder: position:$position item:${item}")

        holder.clView.setOnClickListener {
            clickListener.onItemClicked(position)
        }

        holder.ivSelected.setOnClickListener {
            clickListener.onItemClicked(position)
        }

        holder.btnShow.setOnClickListener {
            val savedPath = item.imagePath
            val intent = Intent(context, ImagesPhotoActivity::class.java)
            intent.putExtra(IMAGE_PATH, savedPath)
            context.startActivity(intent)
        }

        if (item.selected > 0) {
            holder.ivSelected.setImageResource(R.drawable.check_checked)
        } else {
            holder.ivSelected.setImageResource(R.drawable.check_uncheck)
        }

        Glide.with(context)
            .load(item.imagePath)
            .thumbnail(0.1f)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .transition(DrawableTransitionOptions().crossFade())
            .centerCrop() // fitCenter() fitXY()
            .into(holder.ivImage)
    }

    /**
     * 이미지들을 출력할 뷰 홀더
     * @param itemView
     */
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val clView: ConstraintLayout = itemView.findViewById(R.id.recycler_cl_main)
        val ivImage: ImageView = itemView.findViewById(R.id.recycler_iv_image)
        val ivSelected: ImageView = itemView.findViewById(R.id.recycler_iv_selected)
        val btnShow: Button = itemView.findViewById(R.id.recycler_btn_show)
    }
}