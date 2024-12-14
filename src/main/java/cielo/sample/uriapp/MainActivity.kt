package cielo.sample.uriapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cielo.orders.domain.PrinterAttributes
import cielo.orders.domain.SubAcquirer
import cielo.sample.uriapp.databinding.ActivityMainBinding
import cielo.sample.uriapp.domain.GetOrderRequest
import cielo.sample.uriapp.domain.Item
import cielo.sample.uriapp.domain.ListRequest
import cielo.sample.uriapp.domain.Order
import cielo.sample.uriapp.domain.PrintOperation
import cielo.sample.uriapp.domain.PrintRequest
import cielo.sample.uriapp.domain.ProductsRequest
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

private const val TAG = "URIAPP_TEST"

class MainActivity : AppCompatActivity() {

    private var order: Order? = null
    private val alignCenter = HashMap<String, Int>()
    private val alignLeft = HashMap<String, Int>()
    private val alignRight = HashMap<String, Int>()

    private lateinit var binding: ActivityMainBinding

    private val logView get() = binding.logLayout

    private val scheme by lazy { getString(R.string.intent_scheme) }
    private val responseHost by lazy { getString(R.string.intent_host) }
    private val getOrdersHost by lazy { getString(R.string.intent_get_orders_response) }
    private val productsHost by lazy { getString(R.string.intent_products_response) }

    private val callbackUrl by lazy { "$scheme://$responseHost" }

    private val reference get() = "uriapp #" + (System.currentTimeMillis() / 1000)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityMainBinding.inflate(layoutInflater).let {
            binding = it
            setContentView(it.root)
        }

        intent.deserializeQueryParameter("response", Order::class.java) { json, order ->
            Log.d(TAG, "JSON=$json")
            this.order = order
        }

        setStyles()

        binding.checkoutFailureButton.setOnClickListener { btnCheckoutFailureOnClick() }
        binding.checkoutButton.setOnClickListener { btnCheckoutOnClick() }
        binding.checkoutDebitButton.setOnClickListener { btnCheckoutDebitOnClick() }
        binding.printErrorbutton.setOnClickListener { btnPrintErrorOnClick() }
        binding.printButton.setOnClickListener { btnPrintOnClick() }
        binding.printMultipleButton.setOnClickListener { printMultipleOnClick() }
        binding.printImageButton.setOnClickListener { printImageOnClick() }
        binding.btnListOrders.setOnClickListener { btnListOrdersOnClick() }
        binding.btnListOrdersFailure.setOnClickListener { btnListOrdersFailureOnClick() }
        binding.btnTerminalInfo.setOnClickListener { btnTerminalInfoOnClick() }
        binding.btnListProducts.setOnClickListener { btnListProductsOnClick() }
        binding.btnGetOrderById.setOnClickListener { btnGetOrderByIdOnClick() }
        binding.btnGetOrderByValue.setOnClickListener { btnGetOrderByValueOnClick() }
        binding.checkoutButtonWithSubAcquirer.setOnClickListener { makePaymentWithSubAcquirer() }
    }

    private fun btnCheckoutOnClick() {
        makePayment()
    }

    private fun makePaymentWithSubAcquirer() {
        val subAcquirer = SubAcquirer(
            softDescriptor = "TESTE123",
            terminalId = "123A",
            merchantCode = "123B",
            city = "Sao Paulo",
            telephone = "123456789012",
            state = "SP",
            postalCode = "08022300",
            address = "Rua Antonio Bello 22",
            identifier = "123456789B",
            merchantCategoryCode = "5600",
            countryCode = "0076",
            informationType = "F",
            document = "34281869077",
            ibgeCode = "3550308"
        )
        makePayment(subAcquirer)
    }

    private fun makePayment(subAcquirer: SubAcquirer? = null) {
        val price = (500L..1000L).random()
        val quantity = (1..5).random()
        val randomSku: Int = (1000..100000).random()
        val item = Item(
            sku = randomSku.toString(),
            name = "produto",
            unitPrice = price,
            quantity = quantity,
            unitOfMeasure = "unidade"
        )

        val items = mutableListOf(item)
        val request = CheckoutRequest(
            "rSAqNPGvFPJI", "XZevoUYKmkVr",
            price * quantity, null, 1,
            "eduardo.vianna@m4u.com.br", null, reference, items, subAcquirer
        )

        val json: String = Gson().toJson(request).toString()
        Log.d(TAG, "checkout JSON STRING=$json")
        val base64 = getBase64(json)
        val checkoutUri = "lio://payment?request=$base64&urlCallback=$callbackUrl"

        logView.addText("Normal Checkout started $json")

        val i = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUri))
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        try {
            startActivity(i)
        } catch (e: Exception) {
            Log.e(TAG, "ERROR=" + e.toString() + " " + e.message)
            logView.addText("ERROR=" + e.toString() + " " + e.message)
        }
    }

    private fun btnCheckoutDebitOnClick() {
        val price = (500L..1000L).random()
        val quantity = (1..5).random()
        val randomSku: Int = (1000..100000).random()
        val item = Item(
            sku = randomSku.toString(),
            name = "produto",
            unitPrice = price,
            quantity = quantity,
            unitOfMeasure = "unidade"
        )
        val items = mutableListOf<Item>()
        items.add(item)
        val request = CheckoutRequest(
            "rSAqNPGvFPJI", "XZevoUYKmkVr",
            price * quantity, "DEBITO_AVISTA", 0,
            "eduardo.vianna@m4u.com.br", "0010000244470001", reference, items, null
        )

        val json: String = Gson().toJson(request).toString()
        Log.d(TAG, "checkout JSON STRING=$json")
        val base64 = getBase64(json)
        val checkoutUri = "lio://payment?request=$base64&urlCallback=$callbackUrl"

        logView.addText("Normal Checkout started $json")

        val i = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUri))
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        try {
            startActivity(i)
        } catch (e: Exception) {
            Log.e(TAG, "ERROR=" + e.toString() + " " + e.message)
            logView.addText("ERROR=$e ${e.message}")
        }
    }

    private fun printImageOnClick() {

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.cielo)

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)

        val uri = saveImage(bitmap)

        val textsToPrint = arrayOf(uri)
        val style = HashMap<String, Int>()
        val styles = ArrayList<Map<String, Int>>()
        styles.add(style)

        val request = PrintRequest(PrintOperation.PRINT_IMAGE, textsToPrint, styles)
        val json: String = Gson().toJson(request).toString()
        Log.d(TAG, "PRINT IMAGE=$json")
        val base64 = getBase64(json)

        val checkoutUri = "lio://print?request=$base64&urlCallback=$callbackUrl"

        logView.addText("Print Image started $json")

        val i = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUri))
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
    }

    private fun printMultipleOnClick() {

        val textsToPrint =
            arrayOf(
                "Texto alinhado à esquerda.\n\n\n",
                "Texto centralizado\n\n\n",
                "Texto alinhado à direita\n\n\n"
            )
        val styles = ArrayList<Map<String, Int>>()
        styles.add(alignLeft)
        styles.add(alignCenter)
        styles.add(alignRight)
        val request = PrintRequest(PrintOperation.PRINT_MULTI_COLUMN_TEXT, textsToPrint, styles)
        val json: String = Gson().toJson(request).toString()
        Log.d(TAG, "PRINT MULTIPLE STRING=$json")
        val base64 = getBase64(json)
        val checkoutUri = "lio://print?request=$base64&urlCallback=$callbackUrl"

        logView.addText("Multiple Print started $json")

        val i = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUri))
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
    }

    private fun btnPrintOnClick() {

        val style = HashMap<String, Int>()
        val styles = ArrayList<Map<String, Int>>()
        styles.add(style)

        val value: Array<String> = arrayOf("teste impressão\nteste 2 \n teste teste tstesteste")
        val request = PrintRequest(PrintOperation.PRINT_TEXT, value, styles)
        val json: String = Gson().toJson(request).toString()
        Log.d(TAG, "PRINT STRING=$json")
        val base64 = getBase64(json)

        val checkoutUri = "lio://print?request=$base64&urlCallback=$callbackUrl"

        logView.addText("Print started $json")

        val i = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUri))
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
    }

    private fun btnPrintErrorOnClick() {
        val style = HashMap<String, Int>()
        val styles = ArrayList<Map<String, Int>>()
        styles.add(style)
        val value: Array<String> = arrayOf("Teste ERROR")
        val request = PrintRequest(PrintOperation.PRINT_TEXT, value, styles)
        val json: String = Gson().toJson(request).toString()
        Log.d(TAG, "PRINT STRING=$json")
        val base64 = getBase64(json)
        val checkoutUri = "lio://print?request=$base64&urlCallback=invalid://host"

        logView.addText("Print started $json")

        val i = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUri))
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
    }

    private fun btnCheckoutFailureOnClick() {
        val randomSku: Int = (1000..100000).random()
        val randomVal = ThreadLocalRandom.current().nextLong(3, 30)
        val item = Item(
            randomSku.toString(),
            name = "agua",
            unitPrice = 1,
            quantity = 1,
            unitOfMeasure = "unidade"
        )
        val items = mutableListOf<Item>()
        items.add(item)
        val request = CheckoutRequest(
            "rSAqNPGvFPJI", "XZevoUYKmkVr[wrong]", randomVal, "CREDITO_AVISTA", 0,
            "pedro.joppert-impulso@m4u.com.br", "123", reference, items, null
        )
        val json: String = Gson().toJson(request).toString()
        Log.d(TAG, "checkout_failure JSON STRING=$json")

        logView.addText("Failure Checkout started $json")

        val base64 = getBase64(json)
        val checkoutUri = "lio://payment?request=$base64&urlCallback=$callbackUrl"
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUri))
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        try {
            startActivity(i)
        } catch (e: Exception) {
            Log.e(TAG, "ERROR=" + e.toString() + " " + e.message)
            logView.addText("ERROR=$e ${e.message}")
        }
    }

    private fun btnListOrdersOnClick() {
        val request = ListRequest("rSAqNPGvFPJI", "XZevoUYKmkVr", 5, 0)
        val json: String = Gson().toJson(request).toString()

        Log.d(TAG, "checkout JSON STRING = $json")
        val base64 = getBase64(json)
        val checkoutUri = "lio://orders?request=$base64&urlCallback=$scheme://$getOrdersHost"

        logView.addText("Normal Checkout started $json")

        val i = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUri))
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        try {
            startActivity(i)
        } catch (e: Exception) {
            Log.e(TAG, "ERROR=" + e.toString() + " " + e.message)
            logView.addText("ERROR=$e ${e.message}")
        }
    }

    private fun btnListOrdersFailureOnClick() {
        val request = ListRequest("rSAqNPGvFPJI", "XZevoUYKmkVr", 50, 0)
        val json: String = Gson().toJson(request).toString()

        Log.d(TAG, "checkout JSON STRING = $json")
        val base64 = getBase64(json)
        val checkoutUri = "lio://orders?request=$base64&urlCallback=$scheme://$getOrdersHost"

        logView.addText("Normal Checkout started $json")

        val i = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUri))
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        try {
            startActivity(i)
        } catch (e: Exception) {
            Log.e(TAG, "ERROR", e)
            logView.addText("ERROR=$e ${e.message}")
        }
    }

    private fun btnTerminalInfoOnClick() {
        val checkoutUri = "lio://terminalinfo?urlCallback=$callbackUrl"
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUri))
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        try {
            startActivity(i)
        } catch (e: Exception) {
            Log.e(TAG, "ERROR", e)
            logView.addText("ERROR=$e ${e.message}")
        }
    }

    private fun btnListProductsOnClick() {
        val request = ProductsRequest("rSAqNPGvFPJI", "XZevoUYKmkVr")
        val json: String = Gson().toJson(request).toString()
        Log.d(TAG, "checkout JSON STRING = $json")
        val base64 = getBase64(json)

        val checkoutUri = "lio://products?request=$base64&urlCallback=$scheme://$productsHost"
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUri))
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        try {
            startActivity(i)
        } catch (e: Exception) {
            Log.e(TAG, "ERROR", e)
            logView.addText("ERROR=$e ${e.message}")
        }
    }

    private fun btnGetOrderByIdOnClick() {
        if (orderList.isEmpty()) {
            Toast.makeText(applicationContext, " Atualizando lista de Orders!", Toast.LENGTH_SHORT)
                .show()
            btnListOrdersOnClick()
        } else {
            Toast.makeText(applicationContext, " Buscando o Order", Toast.LENGTH_SHORT).show()
            val request = GetOrderRequest(
                orderId = orderList.first().id,
                clientID = "rSAqNPGvFPJI",
                accessToken = "XZevoUYKmkVr"
            )
            val json: String = Gson().toJson(request).toString()
            Log.d(TAG, "checkout JSON STRING = $json")
            val base64 = getBase64(json)

            val checkoutUri = "lio://order?request=$base64&urlCallback=$callbackUrl"
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUri))
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            try {
                startActivity(i)
            } catch (e: Exception) {
                Log.e(TAG, "ERROR", e)
                logView.addText("ERROR=$e ${e.message}")
            }
        }

    }

    private fun btnGetOrderByValueOnClick() {
        if (orderList.isEmpty()) {
            Toast.makeText(applicationContext, " Atualizando lista de Orders!", Toast.LENGTH_SHORT)
                .show()
            btnListOrdersOnClick()
            return
        }
        val selectedOrder: cielo.orders.domain.Order? = findOrderWithPayment(orderList)
        if (selectedOrder == null) {
            Toast.makeText(
                applicationContext,
                "Nenhuma Ordem com Pagamento foi encontrada!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val firstPayment = selectedOrder.payments[0]

        val request = GetOrderRequest(
            amount = firstPayment.amount,
            authCode = firstPayment.authCode,
            cieloCode = firstPayment.cieloCode,
            clientID = "rSAqNPGvFPJI",
            accessToken = "XZevoUYKmkVr"
        )
        val json: String = Gson().toJson(request).toString()
        Log.d(TAG, "checkout JSON STRING = $json")
        val base64 = getBase64(json)

        val checkoutUri = "lio://order?request=$base64&urlCallback=$callbackUrl"
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUri))
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        try {
            startActivity(i)
        } catch (e: Exception) {
            Log.e(TAG, "ERROR", e)
            logView.addText("ERROR=$e ${e.message}")
        }
    }


    private fun setStyles() {
        alignLeft[PrinterAttributes.KEY_ALIGN] = PrinterAttributes.VAL_ALIGN_LEFT
        alignLeft[PrinterAttributes.KEY_TYPEFACE] = 0
        alignLeft[PrinterAttributes.KEY_TEXT_SIZE] = 30

        alignCenter[PrinterAttributes.KEY_ALIGN] = PrinterAttributes.VAL_ALIGN_CENTER
        alignCenter[PrinterAttributes.KEY_TYPEFACE] = 1
        alignCenter[PrinterAttributes.KEY_TEXT_SIZE] = 20

        alignRight[PrinterAttributes.KEY_ALIGN] = PrinterAttributes.VAL_ALIGN_RIGHT
        alignRight[PrinterAttributes.KEY_TYPEFACE] = 2
        alignRight[PrinterAttributes.KEY_TEXT_SIZE] = 15
    }

    private fun getBase64(json: String): String {
        val data = json.toByteArray(Charsets.UTF_8)
        return Base64.encodeToString(data, Base64.DEFAULT)
    }

    private fun saveImage(finalBitmap: Bitmap): String {

        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File("$root/saved_images")
        if (!myDir.exists()) {
            myDir.mkdirs()
        }
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)
        val fname = "Image-$n.jpg"
        val file = File(myDir, fname)
        val path = file.absolutePath
        if (file.exists())
            file.delete()
        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return path
    }

    private fun findOrderWithPayment(resultOrders: List<cielo.orders.domain.Order>): cielo.orders.domain.Order? {
        return resultOrders.firstOrNull { it.payments.isNotEmpty() }
    }

    companion object {
        val orderList: MutableList<cielo.orders.domain.Order> = mutableListOf()
    }


}
