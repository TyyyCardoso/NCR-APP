package ipt.lei.dam.ncrapp.activities.authentication

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BaseActivity
import ipt.lei.dam.ncrapp.models.RecoverPasswordRequest
import ipt.lei.dam.ncrapp.models.SendOTPRequest
import ipt.lei.dam.ncrapp.models.ValidateOTPRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient

class InsertOTPActivity : BaseActivity() {

    private lateinit var countdownTimer: CountDownTimer
    private lateinit var tvCountdown: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_otp)


        val otpEditText = findViewById<EditText>(R.id.etOTP)
        val btnValidateOTP = findViewById<Button>(R.id.btnValidateOTP);
        val backToRecoverPassword = findViewById<Button>(R.id.btnBackToRecoverPassword);

        tvCountdown = findViewById(R.id.tvCountdown)
        tvCountdown.isClickable = false // Inicialmente não clicável

        val userEmailFromLogin = intent.getStringExtra("userInsertedEmail")
        val type = intent.getStringExtra("type")

        backToRecoverPassword.setOnClickListener {
            if(type.equals("1")){
                val intent = Intent(this@InsertOTPActivity, ForgotPasswordActivity::class.java)
                intent.putExtra("userInsertedEmail", userEmailFromLogin)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this@InsertOTPActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

        }

        iniciarContagemRegressiva()

        tvCountdown.setOnClickListener {
            makeRequestWithRetries(
                requestCall = {
                    RetrofitClient.apiService.sendOTP(SendOTPRequest(userEmailFromLogin)).execute()
                },
                onSuccess = { validateOTPResponse ->
                    toast = Toast.makeText(this@InsertOTPActivity, "Código Reenviado", Toast.LENGTH_SHORT)
                    toast!!.show()
                    iniciarContagemRegressiva()
                    setLoadingVisibility(false)
                },
                onError = { errorMessage ->
                    // Tratamento de erro
                    if (toast != null) {
                        toast!!.setText(errorMessage)
                    } else {
                        toast = Toast.makeText(this@InsertOTPActivity, errorMessage, Toast.LENGTH_SHORT)
                    }
                    toast!!.show()
                    setLoadingVisibility(false)
                }
            )

            tvCountdown.isClickable = false // Inicialmente não clicável
        }

        btnValidateOTP.setOnClickListener {

            var validateOTP = true;
            val otpInserted = otpEditText.text.toString()

            if(otpInserted.isNullOrEmpty()){
                otpEditText.error = getString(R.string.insertEmptyOTPBoxError)
                validateOTP = false;
            }

            if(otpInserted.length!=7 && validateOTP){
                otpEditText.error = getString(R.string.insertLenghtOTPBoxError)
                validateOTP = false;
            }


            if(validateOTP){
                setLoadingVisibility(true)
                makeRequestWithRetries(
                    requestCall = {
                        RetrofitClient.apiService.validateOTP(ValidateOTPRequest(otpInserted, userEmailFromLogin, type)).execute()
                    },
                    onSuccess = { validateOTPResponse ->
                        if(type.equals("1")){
                            val intent = Intent(this@InsertOTPActivity, ChangePasswordActivity::class.java)
                            intent.putExtra("userID", validateOTPResponse.userID)
                            startActivity(intent)
                            finish()
                        }else{
                            val intent = Intent(this@InsertOTPActivity, LoginActivity::class.java)
                            startActivity(intent)
                            toast = Toast.makeText(this@InsertOTPActivity, "Conta validada com sucesso. Entre!", Toast.LENGTH_SHORT)
                            toast!!.show()
                            finish()
                        }


                        setLoadingVisibility(false)
                    },
                    onError = { errorMessage ->
                        // Tratamento de erro
                        if (toast != null) {
                            toast!!.setText(errorMessage)
                        } else {
                            toast = Toast.makeText(this@InsertOTPActivity, errorMessage, Toast.LENGTH_SHORT)
                        }
                        toast!!.show()
                        setLoadingVisibility(false)
                    }
                )
            }
        }
    }

    private fun iniciarContagemRegressiva() {
        countdownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                runOnUiThread {
                    tvCountdown.text = "Reenviar código: ${millisUntilFinished / 1000} segundos"
                    tvCountdown.isClickable = false // Mantém não clicável durante a contagem
                }
            }

            override fun onFinish() {
                runOnUiThread {
                    tvCountdown.text = "Clique aqui para reenviar"
                    tvCountdown.isClickable = true // Torna clicável após a contagem terminar
                }
            }
        }.start()
    }
}