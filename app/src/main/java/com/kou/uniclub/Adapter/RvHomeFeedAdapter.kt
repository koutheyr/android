package com.kou.uniclub.Adapter

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.kou.uniclub.Activities.EventDetails
import com.kou.uniclub.Activities.EventDetails.Companion.eventId
import com.kou.uniclub.Extensions.BuilderAuth
import com.kou.uniclub.Extensions.OnBottomReachedListener
import com.kou.uniclub.Model.Event.EventX
import com.kou.uniclub.Model.User.Behaviour.FavoriteResponse
import com.kou.uniclub.Network.UniclubApi
import com.kou.uniclub.R
import com.kou.uniclub.SharedUtils.PrefsManager
import com.kou.uniclub.UI.ImagePreviewer
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.row_event_feed.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

//TODO(" When size =1 && I unparticipated :( buggy bug")
class RvHomeFeedAdapter(val events: ArrayList<EventX>, val context: Context) :
    RecyclerView.Adapter<RvHomeFeedAdapter.Holder>() {
    private var onBottomReachedListener: OnBottomReachedListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): Holder {

        return Holder(
            LayoutInflater.from(parent.context).inflate(
                com.kou.uniclub.R.layout.row_event_feed,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val event: EventX = events[position]
        //date stuff
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = format.parse(event.startTime)
        val day = DateFormat.format("dd", date) as String
        val month = DateFormat.format("MMM", date) as String

        holder.title.text = event.name
        holder.place.text = event.location
        holder.month.text = month
        holder.day.text = day
        if (!event.photo.isEmpty())
            Picasso.get().load(event.photo).into(holder.pic)
        else holder.pic.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.im_event))

        //TODO(" add liked var to EventX and apply notify Itemchanged")
        var isLiked = false
        holder.fav.setOnClickListener {
            if (PrefsManager.geToken(context) != null) {
                if (!isLiked) {
                   like(holder.fav,holder.sparkle,event)

                } else if (isLiked) {
                    unlike(holder.fav, event.id)


                }
                isLiked=!isLiked
            }

            else
                BuilderAuth.showDialog(context)

        }

        //EventO details
        holder.pic.setOnClickListener {
            eventId = event.id
            context.startActivity(Intent(context, EventDetails::class.java))

        }

        holder.pic.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(v: View?): Boolean {
                ImagePreviewer().show(v!!.context, holder.pic)
                return false
            }

        })

        if (position == events.size - 1 && onBottomReachedListener != null) {
            onBottomReachedListener!!.onBottomReached(position)

        }

    }


    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.title!!
        val day = view.day!!
        val month = view.month!!
        val place = view.place!!
        val fav = view.favorite!!
        val pic = view.im_event!!
        val sparkle = view.sparkle!!
        val root = view.rootEvent!!

    }


    fun addData(listItems: ArrayList<EventX>) {
        val size = this.events.size
        val sizeNew = listItems.size

        if (size < sizeNew + size) {
            this.events.addAll(listItems)
            notifyItemRangeInserted(size, sizeNew)
        }


    }

    fun like(view:ImageView,lottie:LottieAnimationView,event:EventX)
    {
        val service = UniclubApi.create()
        service.favorite("Bearer " + PrefsManager.geToken(context)!!, event.id)
            .enqueue(object : Callback<FavoriteResponse> {
                override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                    if (t is IOException)
                        Toasty.warning(context, "Network faillure", Toast.LENGTH_SHORT, true).show()
                }

                override fun onResponse(
                    call: Call<FavoriteResponse>,
                    response: Response<FavoriteResponse>
                ) {
                   lottie.sparkle.playAnimation()
                   view.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorito))
                    Toasty.custom(
                        context,
                        "${event.name} is marked as favourite!",
                        com.kou.uniclub.R.drawable.ic_check_white_24dp,
                        com.kou.uniclub.R.color.toasty,
                        Toasty.LENGTH_SHORT,
                        false,
                        true
                    ).show()
                }

            })
    }

    fun unlike(view: ImageView, id: Int) {
        val service = UniclubApi.create()
        service.unfavorite("Bearer " + PrefsManager.geToken(context)!!, id)
            .enqueue(object : Callback<FavoriteResponse> {
                override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                    Toasty.warning(context, "Network faillure", Toast.LENGTH_SHORT, true).show()
                }

                override fun onResponse(
                    call: Call<FavoriteResponse>,
                    response: Response<FavoriteResponse>
                ) {
                    if (response.isSuccessful) {
                        view.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_favoriteg
                            )
                        )
                    }
                }

            })

    }

    fun setOnBottomReachedListener(listener: OnBottomReachedListener) {

        this.onBottomReachedListener = listener
    }

}
