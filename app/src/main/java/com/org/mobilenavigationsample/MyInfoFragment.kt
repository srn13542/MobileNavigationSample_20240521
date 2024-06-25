package com.org.mobilenavigationsample

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG_BMI = "bmi_fragment"

class MyInfoFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_info, container, false)
        sharedPreferences = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

        val btnSave: Button = view.findViewById(R.id.buttonSave)

        val originalBackground = ContextCompat.getDrawable(requireContext(), R.drawable.edittext_border)
        val focusedBackground = ContextCompat.getDrawable(requireContext(), R.drawable.click_edittext_border)

        val nickname = sharedPreferences.getString("nickname", "")
        val gender = sharedPreferences.getString("gender", "")
        val age = sharedPreferences.getInt("age", 0)
        val height = sharedPreferences.getInt("height", 0)
        val weight = sharedPreferences.getInt("weight", 0)
        val targetWeight = sharedPreferences.getInt("targetWeight", 0)

        val editTextList = listOf<EditText>(
            view.findViewById(R.id.Age),
            view.findViewById(R.id.Height),
            view.findViewById(R.id.Weight),
            view.findViewById(R.id.TargetWeight)
        )
        editTextList.forEach { editText ->
            editText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    editText.background = focusedBackground
                } else {
                    editText.background = originalBackground
                }
            }
        }

        view.findViewById<TextView>(R.id.Nickname).text = nickname
        view.findViewById<RadioButton>(if (gender == "남자") R.id.BtnMan else R.id.BtnWoman).isChecked = true
        view.findViewById<EditText>(R.id.Age).setText(age.toString())
        view.findViewById<EditText>(R.id.Height).setText(height.toString())
        view.findViewById<EditText>(R.id.Weight).setText(weight.toString())
        view.findViewById<EditText>(R.id.TargetWeight).setText(targetWeight.toString())

        val credits_button: View = view.findViewById(R.id.credits_button)
        credits_button.setOnClickListener {
            val intent = Intent(activity, DeveloperCreditsActivity::class.java)
            startActivity(intent)
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userDocRef = firestore.collection("User").document(currentUser.uid)
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        view.findViewById<TextView>(R.id.Nickname).text = document.getString("nickname")
                        val gender = document.getString("gender")
                        if (gender == "남자") {
                            view.findViewById<RadioButton>(R.id.BtnMan).isChecked = true
                        } else {
                            view.findViewById<RadioButton>(R.id.BtnWoman).isChecked = true
                        }
                        view.findViewById<EditText>(R.id.Age).setText(document.getLong("age")?.toString())
                        view.findViewById<EditText>(R.id.Height).setText(document.getLong("height")?.toString())
                        view.findViewById<EditText>(R.id.Weight).setText(document.getLong("weight")?.toString())
                        view.findViewById<EditText>(R.id.TargetWeight).setText(document.getLong("targetWeight")?.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "사용자 정보를 불러오는 데 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnSave.setOnClickListener {
            val editedNickname = view.findViewById<TextView>(R.id.Nickname).text.toString()
            val editedGender = if (view.findViewById<RadioButton>(R.id.BtnMan).isChecked) "남자" else "여자"
            val editedAge = view.findViewById<EditText>(R.id.Age).text.toString().toInt()
            val editedHeight = view.findViewById<EditText>(R.id.Height).text.toString().toInt()
            val editedWeight = view.findViewById<EditText>(R.id.Weight).text.toString().toInt()
            val editedTargetWeight = view.findViewById<EditText>(R.id.TargetWeight).text.toString().toInt()

            // SharedPreferences에 저장
            val editor = sharedPreferences.edit()
            editor.putString("nickname", editedNickname)
            editor.putString("gender", editedGender)
            editor.putInt("age", editedAge)
            editor.putInt("height", editedHeight)
            editor.putInt("weight", editedWeight)
            editor.putInt("targetWeight", editedTargetWeight)
            editor.apply()

            // Firebase Firestore에 저장
            val user = hashMapOf(
                "nickname" to editedNickname,
                "gender" to editedGender,
                "age" to editedAge,
                "height" to editedHeight,
                "weight" to editedWeight,
                "targetWeight" to editedTargetWeight
            )

            if (currentUser != null) {
                firestore.collection("User").document(currentUser.uid)
                    .set(user)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "정보가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        val bmiFragment = BmiFragment.newInstance("", "")
                        parentFragmentManager.beginTransaction().apply {
                            replace(R.id.mainFrameLayout, bmiFragment, TAG_BMI)
                            addToBackStack(TAG_BMI)
                            commit()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "정보 저장에 실패했습니다: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
            } else {
                Toast.makeText(requireContext(), "사용자 인증 실패", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}