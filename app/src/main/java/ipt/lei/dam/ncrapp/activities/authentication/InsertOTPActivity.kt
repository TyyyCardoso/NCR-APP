package ipt.lei.dam.ncrapp.activities.authentication

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BaseActivity
import ipt.lei.dam.ncrapp.models.otp.SendOTPRequest
import ipt.lei.dam.ncrapp.models.otp.ValidateOTPRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient

class InsertOTPActivity : BaseActivity() {

    private lateinit var countdownTimer: CountDownTimer
    private lateinit var tvCountdown: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_otp)

        //Obtidos elementos de UI
        val otpEditText = findViewById<EditText>(R.id.etOTP)
        val btnValidateOTP = findViewById<Button>(R.id.btnValidateOTP)
        val backToRecoverPassword = findViewById<Button>(R.id.btnBackToRecoverPassword)

        tvCountdown = findViewById(R.id.tvCountdown)
        tvCountdown.isClickable = false // Inicialmente não clicável

        val userEmailFromLogin = intent.getStringExtra(getString(R.string.userInsertedEmail))
        val type = intent.getStringExtra(getString(R.string.type))

        //Botão de recuperar password é clicável e leva-nos para uma certa activity dependendo do tipo de operação que estamos a fazer
        /**
         * Se type = 1 - > Recuperar palavra-pass
         * Se type = 2 -> Login
         */
        backToRecoverPassword.setOnClickListener {
            if(type.equals("1")){
                val intent = Intent(this@InsertOTPActivity, ForgotPasswordActivity::class.java)
                intent.putExtra(getString(R.string.userInsertedEmail), userEmailFromLogin)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this@InsertOTPActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

        }

        //Sempre que é enviado um OTP, é iniciada uma contagem regressiva
        iniciarContagemRegressiva()

        /**
         * Quando é clicado na label de reenviar código, é feito um request ao servidor
         */
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

        /**
         * Request ao servidor para validar o códgio inserido pelo cliente
         */
        btnValidateOTP.setOnClickListener {

            var validateOTP = true
            val otpInserted = otpEditText.text.toString()

            //Validações de campos de inserção pelo cliente
            if(otpInserted.isEmpty()){
                otpEditText.error = getString(R.string.insertEmptyOTPBoxError)
                validateOTP = false
            }

            if(otpInserted.length!=7 && validateOTP){
                otpEditText.error = getString(R.string.insertLenghtOTPBoxError)
                validateOTP = false
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

    /**
     *
     * Método "iniciarContagemRegressiva" server para alterar a label que reenvia código para assim que um código for enviado
     * Ter que esperar 30 segundos para enviar outro
     *
     * É criado um objeto do tipo Timer para controlar se já passaram 30 segundos e através de uma thread é verificado se ainda está a contar, ou se já acabou.
     *
     */
    private fun iniciarContagemRegressiva() {
        countdownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                runOnUiThread {
                    tvCountdown.text = getString(R.string.insertOTPCountdownText, millisUntilFinished/1000)
                    tvCountdown.isClickable = false // Mantém não clicável durante a contagem
                }
            }

            override fun onFinish() {
                runOnUiThread {
                    tvCountdown.text = getString(R.string.insertOTPCountdownText2)
                    tvCountdown.isClickable = true // Torna clicável após a contagem terminar
                }
            }
        }.start()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}