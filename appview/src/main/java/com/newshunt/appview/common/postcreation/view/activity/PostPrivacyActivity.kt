package com.newshunt.appview.common.postcreation.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.RadioButton
import com.newshunt.appview.R
import com.newshunt.dataentity.common.asset.PostMeta
import com.newshunt.dataentity.common.asset.PostPrivacy
import com.newshunt.news.view.activity.NewsBaseActivity

const val POST_META_RESULT = "POST_META_RESULT"

class PostPrivacyActivity : NewsBaseActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {

    private lateinit var actionBarBackButton: ImageView
    private lateinit var postPrivacyPublicRadioButton: RadioButton
    private lateinit var postPrivacyPrivateRadioButton: RadioButton
    private lateinit var postAllowCommentsCheckbox: CheckBox
    private var pMeta: PostMeta? = null

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_post_privacy)
        initView()
    }

    private fun initView() {
        actionBarBackButton = findViewById(R.id.actionbar_back_button)
        actionBarBackButton.setOnClickListener(this)
        postPrivacyPublicRadioButton = findViewById(R.id.post_visiblity_public_checkbox)
        postPrivacyPrivateRadioButton = findViewById(R.id.post_visiblity_private_checkbox)
        postAllowCommentsCheckbox = findViewById(R.id.post_allow_comments_checkbox)
        // Enabling the default option.

        if (intent != null && intent.extras != null) {
            pMeta = intent.getSerializableExtra(POST_META_RESULT) as PostMeta
        }

        if (pMeta?.privacyLevel == PostPrivacy.PUBLIC) {
            postPrivacyPublicRadioButton.isChecked = true
            postPrivacyPrivateRadioButton.isChecked = false
        } else if (pMeta?.privacyLevel == PostPrivacy.PRIVATE) {
            postPrivacyPrivateRadioButton.isChecked = true
            postPrivacyPublicRadioButton.isChecked = false
        }
        postAllowCommentsCheckbox.isChecked = pMeta?.allowComments ?:false
        postPrivacyPublicRadioButton.setOnCheckedChangeListener(this)
        postPrivacyPrivateRadioButton.setOnCheckedChangeListener(this)
    }


    override fun onClick(v: View?) {
        when {
            v?.id == R.id.actionbar_back_button -> {
                onBackPressed()
            }
            else -> {

            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when {
            buttonView?.id == R.id.post_visiblity_public_checkbox -> {
                postPrivacyPrivateRadioButton.isChecked = !isChecked
            }
            buttonView?.id == R.id.post_visiblity_private_checkbox -> {
                postPrivacyPublicRadioButton.isChecked = !isChecked
            }
        }
    }

    override fun onBackPressed() {
        handleBackPress()
    }

    private fun handleBackPress() {
        // finishing the activity
        val returnIntent = Intent()
        returnIntent.putExtra(POST_META_RESULT,
                PostMeta(getPostVisibilityState(), postAllowCommentsCheckbox.isChecked))
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    private fun getPostVisibilityState(): PostPrivacy {
        if (postPrivacyPrivateRadioButton.isChecked && !postPrivacyPublicRadioButton.isChecked) {
            return PostPrivacy.PRIVATE
        }
        return PostPrivacy.PUBLIC
    }
}