package com.barterbay.app

import android.R
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.barterbay.app.databinding.FragmentProductNameBinding
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File


class ProductNameFragment : Fragment() {
    private lateinit var viewModel: PostProductViewModel
    private var isTipCardVisible = false
    private var flagForCardViewPreSetting: Int = 0
    private var flagForEditTextCount: Int = 0
    var binding: FragmentProductNameBinding? = null
    var flagForEditText: Int = 0
    var flagForNextButton: Int = 0
    val firebaseStorage = Firebase.storage
    val firebaseStorageRef = firebaseStorage.reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        flagForNextButton++
        binding = FragmentProductNameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(PostProductViewModel::class.java)

        // Simulate button clicks when the fragment is created
        binding!!.button.post {
            binding!!.button.performClick() // First simulated click  /// prakat (aane)

            // Use postDelayed for the second click after 1 second (1000ms)
            binding!!.button.postDelayed({
                binding!!.button.performClick() // Second simulated click     /// jaane
            }, 1)
        }

        // Sets the name saved in viewModel
        if(viewModel.name.value != null){
            binding!!.editTextProductName.text = Editable.Factory.getInstance().newEditable(viewModel.name.value)
        }

        // Sets the lottie file saved in viewModel
        if (viewModel.lottieInNameFragment.value != null) {
            binding!!.lottieView.visibility = View.VISIBLE
            loadLottieAnimation2(viewModel.lottieInNameFragment.value!!)
        }

        binding!!.editTextProductName.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {

                hideKeyboardAndExecuteLogic(v)
                true
            } else {
                false
            }
        }

        // Detect when the BACK button is pressed
        binding!!.editTextProductName.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                hideKeyboardAndExecuteLogic(v)
                true
            } else {
                false
            }
        }

        // Detect when the keyboard is hidden (soft input gone)
        binding!!.root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            binding!!.root.getWindowVisibleDisplayFrame(rect)
            val screenHeight = binding!!.root.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight < screenHeight * 0.15) { // Keyboard is hidden
                executeMyCustomCode()
            }
        }


        binding!!.button.setOnClickListener {
            ++flagForCardViewPreSetting
            if (isTipCardVisible && flagForCardViewPreSetting != 2) { // Regular slide-out with animation
                slideOut2(binding!!.tipCard1)
                slideOut(binding!!.tipCard2)
                slideOut2(binding!!.tipCard3)
//                Toast.makeText(context, "if is executed", Toast.LENGTH_SHORT).show()
            } else if (isTipCardVisible && flagForCardViewPreSetting == 2) { // Instant slide-out (no animation)
                genericMethodToTakeAnimationOneStepAhead(binding!!.tipCard1)
                genericMethodToTakeAnimationOneStepAhead(binding!!.tipCard2)
                genericMethodToTakeAnimationOneStepAhead(binding!!.tipCard3)
//                Toast.makeText(context, "if is executed", Toast.LENGTH_SHORT).show()
            } else { // Slide-in
                binding!!.tipCard1.visibility = View.VISIBLE
                slideIn2(binding!!.tipCard1)
                slideIn(binding!!.tipCard2)
                slideIn2(binding!!.tipCard3)
//                Toast.makeText(context, "else is executed", Toast.LENGTH_SHORT).show()
            }
            isTipCardVisible = !isTipCardVisible
        }

//        binding!!.editTextProductName.setOnClickListener() {
//            Toast.makeText(context, "EditText is triggered", Toast.LENGTH_SHORT).show()
//            ++flagForEditTextCount
//            if(flagForEditTextCount == 1) {
//                binding!!.tipCard1.visibility = View.VISIBLE
//                slideIn2(binding!!.tipCard1)
//                slideIn(binding!!.tipCard2)
//                slideIn2(binding!!.tipCard3)
////                Toast.makeText(context, "else is executed", Toast.LENGTH_SHORT).show()
//
//                isTipCardVisible = !isTipCardVisible
//            }
//        }

        if (flagForNextButton > 1) {
            binding!!.submitBtn.visibility = View.VISIBLE
        }


        binding!!.submitBtn.setOnClickListener {

            if (!isProductNameValid()) {
                binding!!.editTextProductName.error = "Product name is required"
                binding!!.editTextProductName.requestFocus()
                Toast.makeText(context, "Please enter a product name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else {
//            if (isTipCardVisible && flagForCardViewPreSetting != 2) { // Regular slide-out with animation
                slideOut2(binding!!.tipCard1)
                slideOut(binding!!.tipCard2)
                slideOut2(binding!!.tipCard3)
//            }
                viewModel.name.value = binding!!.editTextProductName.text.toString()
                viewModel.lottieInNameFragment.value = currentLottieFile    // string value

                lifecycleScope.launch {     // modern & efficient, threading, (coroutines)
                    delay(1000)
                    findNavController().navigate(com.barterbay.app.R.id.action_name_to_category)
                }

//            else { // Slide-in
//                binding!!.tipCard1.visibility = View.VISIBLE
//                slideIn2(binding!!.tipCard1)
//                slideIn(binding!!.tipCard2)
//                slideIn2(binding!!.tipCard3)
//            }
//            isTipCardVisible = !isTipCardVisible
            }
        }

        binding!!.editTextProductName.setOnFocusChangeListener { kuchbhi, changeHuaHainYaNahiHuaHain ->
            if (flagForEditText < 1) {
                cardViewSlideIn(changeHuaHainYaNahiHuaHain) // Call only if flag is less than 1
                flagForEditText++ // Increment flag

                binding!!.submitBtn.apply {
                    visibility = View.VISIBLE
                    alpha = 0f
                    animate()
                        .alpha(1f)
                        .setDuration(500) // Duration in milliseconds
                        .setInterpolator(AccelerateDecelerateInterpolator()) // Smooth transition
                        .start()
                }

            }
        }

//        binding!!.editTextProductName.addTextChangedListener(object : TextWatcher{
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//
//                // earlier we used the code from executCustomLogic() here to continuously check for the lottie animations but later we dismissed it because it started to cause the main UI thread to get overheads even if I made another threads for each request of the lottie animation from firebase.
//
//            }
//
//        })


        return binding!!.root
    }

    private fun isProductNameValid(): Boolean {
        val name = binding!!.editTextProductName.text?.toString()?.trim()
        return !name.isNullOrEmpty() && name.length >= 3  // minimum 3 characters

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)  // Set whether home should be displayed as an "up" affordance. Set this to true if selecting "home" returns up by a single level in your UI rather than back to the top level or front page

        // For AutoCompleteTextView
        val productListAutoComplete = listOf<String>(
            "Samsung Galaxy S21 (128GB, Phantom Gray) (Smartphones)",
            "Samsung Galaxy S22 Ultra (256GB, Phantom Black) (Smartphones)",
            "Samsung Galaxy A53 (128GB, Awesome Blue) (Smartphones)",
            "Xiaomi Redmi Note 12 Pro (5G, Glacier White) (Smartphones)",
            "Xiaomi Mi 11 Ultra (256GB, Ceramic Black) (Smartphones)",
            "OnePlus 11 (128GB, Eternal Green) (Smartphones)",
            "OnePlus Nord CE 3 Lite (8GB RAM, Pastel Lime) (Smartphones)",
            "Apple iPhone 14 Pro (256GB, Space Black) (Smartphones)",
            "Apple iPhone SE (2022, 128GB, Starlight) (Smartphones)",
            "Google Pixel 7 Pro (256GB, Hazel Green) (Smartphones)",
            "Realme Narzo 60 (128GB, Cosmic Purple) (Smartphones)",
            "Vivo V27 (8GB RAM, Magic Blue) (Smartphones)",
            "Oppo Reno8 Pro (5G, Glazed Green) (Smartphones)",
            "Motorola Edge 40 (128GB, Eclipse Black) (Smartphones)",
            "Asus ROG Phone 7 (16GB RAM, Storm White) (Smartphones)",
            "Samsung Galaxy Z Fold 5 (512GB, Cream) (Smartphones)",
            "Xiaomi Redmi K60 (12GB RAM, Shadow Grey) (Smartphones)",
            "Sony Xperia 1 V (4K OLED, Black) (Smartphones)",
            "Nokia XR21 (Rugged, 5G, Ice White) (Smartphones)",
            "Huawei P60 Pro (512GB, Rococo Pearl) (Smartphones)",
            "Apple Watch Series 9 (41mm, Midnight Aluminum) (Fashion Watches)",
            "Samsung Galaxy Watch 6 (44mm, Silver) (Fashion Watches)",
            "Fossil Gen 6 Smartwatch (Stainless Steel, Black) (Fashion Watches)",
            "Casio G-Shock DW5600E (Classic Digital) (Fashion Watches)",
            "Rolex Submariner Date (Green Dial, Oystersteel) (Fashion Watches)",
            "Tissot PRX (Powermatic 80, Blue Dial) (Fashion Watches)",
            "Seiko 5 Sports (Automatic, Black Bezel) (Fashion Watches)",
            "Omega Speedmaster Moonwatch (Professional, Steel) (Fashion Watches)",
            "Garmin Fenix 7 (Sapphire Solar, Black Titanium) (Fashion Watches)",
            "Timex Weekender (36mm, Brown Leather Strap) (Fashion Watches)",
            "Apple MacBook Pro (M2, 16GB RAM, 512GB SSD) (Apple MacBook Variants)",
            "Apple MacBook Air (M1, 8GB RAM, Space Gray) (Apple MacBook Variants)",
            "Dell XPS 13 (9315, Platinum Silver, 16GB RAM) (Apple MacBook Variants)",
            "HP Spectre x360 (14-inch, OLED Touch Display) (Apple MacBook Variants)",
            "Asus ZenBook Duo (Dual Screen, 1TB SSD) (Apple MacBook Variants)",
            "Lenovo ThinkPad X1 Carbon (Gen 11, Black) (Apple MacBook Variants)",
            "Microsoft Surface Laptop 5 (Platinum, 512GB SSD) (Apple MacBook Variants)",
            "Acer Predator Helios 300 (16GB RAM, RTX 3060) (Apple MacBook Variants)",
            "MSI GF63 Thin (11th Gen, 8GB RAM, 256GB SSD) (Apple MacBook Variants)",
            "Razer Blade 15 (2023, QHD, 240Hz) (Apple MacBook Variants)",
            "HP Pavilion Gaming (12GB RAM, GTX 1650) (Apple MacBook Variants)",
            "Dell Inspiron 15 (512GB SSD, Core i7) (Apple MacBook Variants)",
            "Asus TUF Gaming F15 (Intel i5, 144Hz Display) (Apple MacBook Variants)",
            "Gigabyte Aero 15 (OLED 4K, 1TB SSD) (Apple MacBook Variants)",
            "LG Gram 16 (Ultra-light, Intel Evo) (Apple MacBook Variants)",
            "Nike Air Force 1 (Triple White, Low Top) (Fashion Shoes)",
            "Nike Air Max 270 (Black and White) (Fashion Shoes)",
            "Nike Jordan Retro 4 (Bred) (Fashion Shoes)",
            "Adidas Ultraboost 22 (Core Black) (Fashion Shoes)",
            "Adidas Yeezy 350 V2 (Mono Mist) (Fashion Shoes)",
            "Puma RS-X3 (Multicolor Sneakers) (Fashion Shoes)",
            "Reebok Nano X1 (Training Shoes, Blue) (Fashion Shoes)",
            "Skechers Go Walk 5 (Walking Shoes, Navy) (Fashion Shoes)",
            "New Balance 574 (Classic Gray) (Fashion Shoes)",
            "Under Armour HOVR Phantom 3 (Running Shoes) (Fashion Shoes)",
            "Canon EOS R5 (Full-Frame Mirrorless, 45MP) (Cameras)",
            "Nikon Z7 II (Mirrorless, 64GB SD Card Bundle) (Cameras)",
            "Sony Alpha a7 III (ILCE-7M3K, 24.2MP) (Cameras)",
            "Fujifilm X-T5 (Mirrorless, Silver, 40.2MP) (Cameras)",
            "Panasonic Lumix S5 (4K, L-Mount Lens Kit) (Cameras)",
            "Olympus OM-D E-M1 Mark III (Pro Kit) (Cameras)",
            "GoPro Hero 11 Black (Action Camera, 5.3K Video) (Cameras)",
            "Insta360 X3 (360-Degree Video, Waterproof) (Cameras)",
            "DJI Osmo Pocket 3 (Handheld Stabilized Camera) (Cameras)",
            "Canon PowerShot G7 X Mark III (Compact Vlogging) (Cameras)",
            "Ray-Ban Aviator (Gold Frame, Green Lens) (Fashion Sunglasses)",
            "Ray-Ban Wayfarer (Classic Black, Polarized) (Fashion Sunglasses)",
            "Oakley Holbrook (Matte Black, Prizm Lens) (Fashion Sunglasses)",
            "Fastrack Square Sunglasses (UV Protection, Brown) (Fashion Sunglasses)",
            "Carrera Rectangle Sunglasses (Black and Gold) (Fashion Sunglasses)",
            "Polaroid Round Sunglasses (Gradient Blue) (Fashion Sunglasses)",
            "Vogue Cat Eye Sunglasses (Tortoise Shell) (Fashion Sunglasses)",
            "Armani Exchange Sunglasses (Oversized, Pink) (Fashion Sunglasses)",
            "Yamaha Acoustic Guitar (Model F280, Natural) (Musical Instruments)",
            "Fender Stratocaster (Electric Guitar, Red) (Musical Instruments)",
            "Casio CT-X700 (Keyboard, 61 Keys) (Musical Instruments)",
            "Roland TD-17KVX (Electronic Drum Kit) (Musical Instruments)",
            "Epiphone Les Paul (Standard PlusTop Pro) (Musical Instruments)",
            "Gibson SG Standard (Heritage Cherry Finish) (Musical Instruments)",
            "Ibanez GSR200 (Bass Guitar, Black) (Musical Instruments)",
            "Yamaha PSR-E373 (Portable Keyboard) (Musical Instruments)",
            "Pearl Roadshow (Drum Kit, Jet Black) (Musical Instruments)",
            "Hohner Marine Band (Harmonica, Key of C) (Musical Instruments)",
            "Honda Activa 6G (2023, Pearl White) (Vehicles)",
            "Honda CB Hornet 160R (Red, Dual ABS) (Vehicles)",
            "Bajaj Pulsar NS200 (Black, BS6) (Vehicles)",
            "Royal Enfield Classic 350 (Halcyon Green) (Vehicles)",
            "TVS Apache RTR 200 4V (White, SmartXonnect) (Vehicles)",
            "Suzuki Access 125 (Special Edition, Blue) (Vehicles)",
            "KTM Duke 390 (Orange, ABS) (Vehicles)",
            "Yamaha MT-15 (2023, Metallic Black) (Vehicles)",
            "Hero Splendor Plus (100cc, Black) (Vehicles)",
            "Harley-Davidson Street 750 (Vivid Black) (Vehicles)",
            "LG Smart Refrigerator (Side by Side, 675L) (Home Appliances)",
            "Samsung 55-inch QLED TV (4K, Smart) (Home Appliances)",
            "Whirlpool Washing Machine (7kg, Fully Automatic) (Home Appliances)",
            "Dyson V11 Vacuum Cleaner (Cordless) (Home Appliances)",
            "Philips Air Fryer (Rapid Air Technology, Black) (Home Appliances)",
            "Panasonic Microwave Oven (27L, Convection) (Home Appliances)",
            "Hitachi Inverter AC (1.5 Ton, Split) (Home Appliances)",
            "Sony Home Theater System (5.1 Channel) (Home Appliances)",
            "Prestige Induction Cooktop (2000W, Black) (Home Appliances)",
            "Havells Ceiling Fan (Remote Control, White) (Home Appliances)",
            "Harry Potter and the Sorcerer's Stone (Paperback) (Books)",
            "Atomic Habits by James Clear (Hardcover) (Books)",
            "The Alchemist by Paulo Coelho (Paperback) (Books)",
            "Sapiens by Yuval Noah Harari (Illustrated) (Books)",
            "Rich Dad Poor Dad by Robert Kiyosaki (20th Anniversary Edition) (Books)",
            "The Subtle Art of Not Giving a F*ck (Mark Manson) (Books)",
            "1984 by George Orwell (Dystopian Classic) (Books)",
            "The Psychology of Money (Morgan Housel) (Books)",
            "To Kill a Mockingbird (Harper Lee, Anniversary Edition) (Books)",
            "Pride and Prejudice by Jane Austen (Hardcover) (Books)",
            "Engineering Thermodynamics Notes (3rd Semester) (Engineering Notes)",
            "Civil Engineering Structural Analysis Notes (Engineering Notes)",
            "Digital Electronics Handwritten Notes (Engineering Notes)",
            "Engineering Mathematics-1 Formula Book (Engineering Notes)",
            "Mechanics of Materials Reference Notes (Engineering Notes)",
            "Signals and Systems Problem Solving Guide (Engineering Notes)",
            "Machine Design Key Concept Notes (Engineering Notes)",
            "Fluid Mechanics Summary Notes (Engineering Notes)",
            "Control Systems Practice Notes (Engineering Notes)",
            "Electrical Circuit Analysis Notes (Engineering Notes)",
            "Apple iPad Air (5th Gen, 256GB, Wi-Fi) (Tablets)",
            "Samsung Galaxy Tab S8 Ultra (5G, 128GB) (Tablets)",
            "Lenovo Tab P11 Pro (2nd Gen, OLED) (Tablets)",
            "Microsoft Surface Pro 9 (Intel i7, 256GB) (Tablets)",
            "Xiaomi Pad 6 Pro (8GB RAM, 128GB Storage) (Tablets)",
            "Realme Pad X (5G, 64GB, Glacier Blue) (Tablets)",
            "Amazon Fire HD 10 (Kids Edition, 32GB) (Tablets)",
            "Asus ROG Flow Z13 (Gaming Tablet, 1TB SSD) (Tablets)",
            "TCL TAB 10s (Educational Tablet, Stylus Included) (Tablets)",
            "Huawei MatePad 11 (120Hz Display, 6GB RAM) (Tablets)",
            "Anker PowerPort III Nano (20W USB-C Charger) (Chargers)",
            "Apple MagSafe Duo Charger (Foldable Design) (Chargers)",
            "Samsung 45W Super Fast Charger (USB-C Adapter) (Chargers)",
            "Belkin BoostCharge Pro (3-in-1 Wireless Charger) (Chargers)",
            "Aukey Omnia Mini 65W PD Charger (Chargers)",
            "UGREEN 100W GaN Charger (4-Port USB-C Hub) (Chargers)",
            "Spigen ArcStation Pro 40W (Dual Port Adapter) (Chargers)",
            "Baseus 120W GaN Fast Charger (Multi-Port) (Chargers)",
            "OnePlus Warp Charge 65W Adapter (Chargers)",
            "Xiaomi Mi 33W SonicCharge Adapter (Chargers)",
            "Anker PowerCore 20,000mAh (Fast Charging) (Power Banks)",
            "Mi Power Bank 3i (10,000mAh, Dual Port) (Power Banks)",
            "Realme Power Bank 2 (20W, Quick Charge) (Power Banks)",
            "Ambrane Stylo 20K (20,000mAh) (Power Banks)",
            "Samsung Wireless Power Bank (10,000mAh) (Power Banks)",
            "Cygnett ChargeUp Pro 27K (Laptop Power Bank) (Power Banks)",
            "Aukey PB-Y36 (36,000mAh) (Power Banks)",
            "RavPower PD Pioneer 26,800mAh (USB-C) (Power Banks)",
            "Belkin Pocket Power (15,000mAh) (Power Banks)",
            "Lenovo PB420 (14,000mAh, Dual Input) (Power Banks)",
            "Logitech MX Keys (Wireless, Backlit) (Keyboards)",
            "Corsair K70 RGB MK.2 (Mechanical Gaming Keyboard) (Keyboards)",
            "Razer BlackWidow V4 Pro (Mechanical, RGB) (Keyboards)",
            "Microsoft Surface Keyboard (Slim and Wireless) (Keyboards)",
            "Apple Magic Keyboard (Rechargeable, White) (Keyboards)",
            "Keychron K3 (Low Profile, Bluetooth) (Keyboards)",
            "SteelSeries Apex Pro (OLED Display, Adjustable Actuation) (Keyboards)",
            "Redragon K552 (Compact Gaming Keyboard) (Keyboards)",
            "HP Pavilion Wired Keyboard 500 (Keyboards)",
            "Asus ROG Claymore II (Hot-Swappable Keys) (Keyboards)",
            "Sony WH-1000XM5 (Noise-Canceling Headphones) (Calculators)",
            "Bose QuietComfort 45 (Wireless Over-Ear) (Calculators)",
            "Apple AirPods Max (Spatial Audio, Blue) (Calculators)",
            "Sennheiser HD 660S (Open-Back Audiophile) (Calculators)",
            "JBL Tune 760NC (Active Noise Cancellation) (Calculators)",
            "Audio-Technica ATH-M50X (Studio Monitor Headphones) (Calculators)",
            "Beats Studio3 (Wireless, Pure ANC) (Calculators)",
            "Skullcandy Crusher Evo (Bass Adjustability) (Calculators)",
            "Shure SRH1540 (Premium Closed-Back Headphones) (Calculators)",
            "Razer Kraken V3 Pro (Wireless Gaming Headset) (Calculators)",
            "Casio FX-991EX ClassWiz (Scientific Calculator) (Backpacks)",
            "Texas Instruments TI-84 Plus (Graphing Calculator) (Backpacks)",
            "HP 35S (Scientific Programmable Calculator) (Backpacks)",
            "Sharp EL-W516XBSL (Advanced Scientific Calculator) (Backpacks)",
            "Canon F-789SGA (Solar Powered Calculator) (Backpacks)",
            "Casio FX-CG50 (Graphing, High-Resolution Display) (Backpacks)",
            "TI-Nspire CX II (CAS Graphing Calculator) (Backpacks)",
            "Casio MS-10VC (Compact Basic Calculator) (Backpacks)",
            "Aurora AX-595TV (Twin Power Scientific) (Backpacks)",
            "Victor 940-4 (Desktop Calculator) (Backpacks)",
            "Wildcraft Supernova 40L (Hiking Backpack) (Furniture Desks and Chairs)",
            "Nike Brasilia Training Backpack (Black) (Furniture Desks and Chairs)",
            "Samsonite Tectonic Lifestyle (Laptop Backpack) (Furniture Desks and Chairs)",
            "American Tourister Jet Backpack (Blue) (Furniture Desks and Chairs)",
            "North Face Recon (Laptop and Travel) (Furniture Desks and Chairs)",
            "Herschel Little America (Signature Backpack) (Furniture Desks and Chairs)",
            "HP Odyssey Backpack (15.6-inch Laptop Bag) (Furniture Desks and Chairs)",
            "Puma Phase Backpack (School and College) (Furniture Desks and Chairs)",
            "Fjällräven Kånken (Classic Travel Bag) (Furniture Desks and Chairs)",
            "Decathlon Quechua 30L Hiking Backpack (Furniture Desks and Chairs)",
            "Urban Ladder Study Desk (Solid Wood, Walnut Finish) (Mattresses)",
            "Nilkamal Freedom Mini Desk (Plastic, Compact Design) (Mattresses)",
            "Ikea Micke Desk (White, Minimalist) (Mattresses)",
            "AmazonBasics Height Adjustable Standing Desk (Mattresses)",
            "GreenSoul Ergonomic Chair (Mesh Back) (Mattresses)",
            "Herman Miller Aeron Chair (Graphite Finish) (Mattresses)",
            "Featherlite Liberate Office Chair (Adjustable Arms) (Mattresses)",
            "Godrej Interio Slimline Chair (Premium Comfort) (Mattresses)",
            "Furinno Simple Design Writing Desk (Mattresses)",
            "Wipro Adapt Chair (Work-from-Home Essentials) (Mattresses)",
            "Wakefit Orthopedic Memory Foam Mattress (Queen Size) (Room Decor)",
            "Sleepwell SleepX Dual Comfort Mattress (Reversible) (Room Decor)",
            "Kurlon Dream Sleep Mattress (Spring, 5-inch) (Room Decor)",
            "Duroflex LiveIn Memory Foam Roll Mattress (Room Decor)",
            "Sleepyhead Flip Dual Mattress (Medium Soft) (Room Decor)",
            "Peps Spine Guard Mattress (Ortho Care) (Room Decor)",
            "Flo Ergo Mattress (Cool Gel-Infused Foam) (Room Decor)",
            "Springtek Ortho Queen Mattress (Latex Layer) (Room Decor)",
            "Coirfit Health+ Mattress (Hybrid) (Room Decor)",
            "Emma Ortho Mattress (European Standard) (Room Decor)",
            "Philips Hue White & Color Ambiance Smart Bulb (Bulbs)",
            "Ikea Lack Wall Shelf (White Finish) (Bulbs)",
            "Urban Ladder Wooden Wall Mirror (Teak Finish) (Bulbs)",
            "Artistry Wall Hanging Macrame (Handwoven) (Bulbs)",
            "Godrej Interio Soft Glow Table Lamp (Bulbs)",
            "Nilkamal Bean Bag (XXL, Leatherette) (Bulbs)",
            "Decorative String Fairy Lights (Warm White) (Bulbs)",
            "Abstract Canvas Art (Set of 3 Panels) (Bulbs)",
            "Peacock Feather Dream Catcher (Multicolor) (Bulbs)",
            "Velvet Floor Cushions (Set of 2) (Bulbs)",
            "SG Test Cricket Bat (English Willow) (Sports Equipment)",
            "Yonex Nanoray 18i Badminton Racket (Sports Equipment)",
            "Nike Flight Soccer Ball (FIFA Approved) (Sports Equipment)",
            "Cosco CB-120 Basketball (Indoor/Outdoor) (Sports Equipment)",
            "Nivia Storm Volleyball (Sports Equipment)",
            "Stiga Advantage Table Tennis Table (Sports Equipment)",
            "Speedo Fastskin Goggles (Swimming) (Sports Equipment)",
            "Adidas Combat Boxing Gloves (12oz) (Sports Equipment)",
            "Decathlon Kipsta Sports Shoes (Football) (Sports Equipment)",
            "Head Graphene 360+ Tennis Racket (Sports Equipment)",
            "Hero Lectro F6i Electric Bicycle (Bicycles)",
            "Hercules Roadeo A75 Mountain Bike (Bicycles)",
            "B’Twin Riverside 500 Hybrid Bike (Bicycles)",
            "Firefox Viper D Mountain Bicycle (Bicycles)",
            "Trek FX 2 Disc Hybrid Cycle (Bicycles)",
            "Montra Helicon Disc (27.5-Inch MTB) (Bicycles)",
            "Schwinn Discover Hybrid Bike (21-Speed) (Bicycles)",
            "Giant Talon 29 3 MTB (Aluminum Frame) (Bicycles)",
            "Cannondale Quick CX 3 (Hybrid Bike) (Bicycles)",
            "Scott Aspect 970 Mountain Bike (Bicycles)",
            "HP 803 Ink Cartridge (Black) (Printer Accessories)",
            "Canon CL-57 Color Ink Cartridge (Printer Accessories)",
            "Epson T6641 Ink Bottle (Black, EcoTank) (Printer Accessories)",
            "Brother TN-2335 Toner Cartridge (Printer Accessories)",
            "Samsung MLT-D111S Toner Cartridge (Printer Accessories)",
            "HP Paper Feeder Tray (500 Sheets) (Printer Accessories)",
            "Xerox Drum Cartridge (WorkCentre 3215) (Printer Accessories)",
            "Epson L3150 Printer Ribbon Set (Printer Accessories)",
            "Canon Maintenance Cartridge MC-20 (Printer Accessories)",
            "Kodak Glossy Photo Paper (4x6 Inches) (Printer Accessories)",
            "Classmate Pulse Spiral Notebook (300 Pages) (Stationery (Notebooks, Pens))",
            "Parker Jotter Stainless Steel Ball Pen (Stationery (Notebooks, Pens))",
            "Moleskine Classic Notebook (Hardcover, Ruled) (Stationery (Notebooks, Pens))",
            "Faber-Castell Grip Mechanical Pencil (0.7mm) (Stationery (Notebooks, Pens))",
            "Camlin Kokuyo Artist Sketchbook (A4 Size) (Stationery (Notebooks, Pens))",
            "Lamy Safari Fountain Pen (Blue Ink) (Stationery (Notebooks, Pens))",
            "Paperkraft Premium Executive Diary (Stationery (Notebooks, Pens))",
            "Pilot V7 Hi-Techpoint Pen (Pack of 5) (Stationery (Notebooks, Pens))",
            "Staedtler Fineliner Pen Set (20 Colors) (Stationery (Notebooks, Pens))",
            "Bilt Matrix A4 Copier Paper (500 Sheets) (Stationery (Notebooks, Pens))",
            "Microsoft Office 365 Home Subscription (1 Year) (Software Licenses)",
            "Adobe Creative Cloud All Apps Plan (1 Year) (Software Licenses)",
            "Norton 360 Deluxe (Antivirus, 5 Devices) (Software Licenses)",
            "Autodesk AutoCAD 2025 (Single-User License) (Software Licenses)",
            "CorelDRAW Graphics Suite 2025 (Lifetime) (Software Licenses)",
            "Grammarly Premium Subscription (1 Year) (Software Licenses)",
            "McAfee Total Protection (10 Devices) (Software Licenses)",
            "JetBrains IntelliJ IDEA Ultimate License (Software Licenses)",
            "Kaspersky Internet Security (3 Devices) (Software Licenses)",
            "Sketch Pro (UI/UX Design Tool, 1 Year) (Software Licenses)",
            "Epson EH-TW7100 4K Projector (Projectors)",
            "BenQ TK850i 4K HDR Smart Projector (Projectors)",
            "ViewSonic PX701HD Full HD Projector (Projectors)",
            "Sony VPL-VW295ES 4K Home Theater Projector (Projectors)",
            "LG HU810PW CineBeam 4K UHD Laser Projector (Projectors)",
            "Optoma UHD38 (Gaming Projector, 240Hz) (Projectors)",
            "Xiaomi Mi Smart Projector 2 Pro (Compact) (Projectors)",
            "Anker Nebula Capsule II Mini Projector (Projectors)",
            "Dell Advanced Laser Projector (S718QL) (Projectors)",
            "Acer H7850 4K Ultra HD Projector (Projectors)",
            "Dell UltraSharp U2723QE 4K Monitor (27-Inch) (Monitors)",
            "LG UltraGear 27GN950 (144Hz Gaming Monitor) (Monitors)",
            "Samsung Odyssey G7 (32-Inch Curved Gaming Monitor) (Monitors)",
            "Asus ProArt Display PA32UCX (HDR Monitor) (Monitors)",
            "BenQ EX3501R (35-Inch Ultra-Wide Monitor) (Monitors)",
            "Acer Predator XB273U (165Hz QHD Gaming Monitor) (Monitors)",
            "HP Pavilion 27Q (4K Display) (Monitors)",
            "Lenovo ThinkVision P32p-30 (32-Inch Monitor) (Monitors)",
            "MSI Optix MAG274QRF-QD (Gaming Display) (Monitors)",
            "ViewSonic VX3276-MHD (32-Inch Frameless Monitor) (Monitors)",
            "Seagate Backup Plus Portable (5TB) (Hard Drives)",
            "WD My Passport Ultra (4TB, USB-C) (Hard Drives)",
            "Toshiba Canvio Basics (2TB, External HDD) (Hard Drives)",
            "LaCie Rugged Mini (1TB, Portable HDD) (Hard Drives)",
            "Samsung T7 Shield (2TB SSD) (Hard Drives)",
            "SanDisk Extreme Pro Portable SSD (1TB) (Hard Drives)",
            "G-Technology G-Drive (4TB, USB 3.0) (Hard Drives)",
            "Kingston XS2000 External SSD (2TB) (Hard Drives)",
            "ADATA HD710 Pro (Waterproof, 1TB HDD) (Hard Drives)",
            "Transcend StoreJet 25M3 (Shockproof, 2TB) (Hard Drives)",
            "SanDisk Ultra Dual Drive Luxe (128GB, USB-C) (Flash Drives)",
            "Kingston DataTraveler Kyson (256GB USB 3.2) (Flash Drives)",
            "Samsung BAR Plus Flash Drive (64GB) (Flash Drives)",
            "HP x796w USB Flash Drive (1TB) (Flash Drives)",
            "Corsair Voyager Slider X1 (128GB) (Flash Drives)",
            "PNY Turbo USB 3.0 Flash Drive (64GB) (Flash Drives)",
            "Transcend JetFlash 790 (128GB USB 3.1) (Flash Drives)",
            "Lexar JumpDrive S57 (256GB) (Flash Drives)",
            "Verbatim PinStripe USB Flash Drive (32GB) (Flash Drives)",
            "Team Group C212 USB 3.2 Gen 2 (512GB) (Flash Drives)",
            "Sony PlayStation 5 (Disc Edition, 825GB) (Gaming Consoles)",
            "Microsoft Xbox Series X (1TB SSD) (Gaming Consoles)",
            "Nintendo Switch OLED Model (64GB Storage) (Gaming Consoles)",
            "PlayStation 4 Pro (1TB Console) (Gaming Consoles)",
            "Xbox Series S (512GB Digital) (Gaming Consoles)",
            "Nintendo Switch Lite (Yellow Edition) (Gaming Consoles)",
            "Steam Deck (512GB Gaming Handheld) (Gaming Consoles)",
            "Razer Edge 5G Gaming Handheld (Gaming Consoles)",
            "Sega Genesis Mini 2 (Retro Console) (Gaming Consoles)",
            "Logitech G Cloud Gaming Console (Gaming Consoles)",
            "Monopoly Deal Card Game (Classic Edition) (Board Games)",
            "Catan (Board Game for Strategy Lovers) (Board Games)",
            "Ticket to Ride Europe Edition (Board Games)",
            "Risk (Global Domination Board Game) (Board Games)",
            "Scrabble Deluxe Edition (Wooden Tiles) (Board Games)",
            "Chess Set with Glass Pieces (Board Games)",
            "Carcassonne (Tile Placement Game) (Board Games)",
            "Pictionary Air (Interactive Drawing Game) (Board Games)",
            "Clue (Mystery Solving Board Game) (Board Games)",
            "Betrayal at Baldur’s Gate (Horror Themed) (Board Games)",
            "Faber-Castell Polychromos Colored Pencils (Set of 36) (Art Supplies)",
            "Canson XL Watercolor Pad (9x12 Inches) (Art Supplies)",
            "Winsor & Newton Cotman Watercolor Set (24 Colors) (Art Supplies)",
            "Sakura Pigma Micron Fineliner Pens (Set of 8) (Art Supplies)",
            "Prismacolor Premier Colored Pencils (72-Pack) (Art Supplies)",
            "Mont Marte Acrylic Paint Set (24 Colors) (Art Supplies)",
            "Derwent Graphite Sketching Pencils (12 Pieces) (Art Supplies)",
            "Arteza 36-Piece Brush Set (Synthetic Hair) (Art Supplies)",
            "Strathmore 400 Series Drawing Pad (11x14 Inches) (Art Supplies)",
            "Staedtler Pigment Liner Pen Set (Art Supplies)",
            "Singer Heavy Duty 4423 Sewing Machine (Sewing Machines)",
            "Brother CS6000i Sewing and Quilting Machine (Sewing Machines)",
            "Janome HD3000 Heavy-Duty Sewing Machine (Sewing Machines)",
            "Usha Janome Dream Stitch Automatic Sewing Machine (Sewing Machines)",
            "Bernette 38 Swiss Design Sewing Machine (Sewing Machines)",
            "Juki HZL-F600 Computerized Sewing Machine (Sewing Machines)",
            "Brother XM2701 Lightweight Full-Featured Sewing Machine (Sewing Machines)",
            "Elna eXcellence 680+ Sewing Machine (Sewing Machines)",
            "Singer Start 1306 Basic Sewing Machine (Sewing Machines)",
            "Bernina 325 Compact Sewing Machine (Sewing Machines)",
            "Philips Viva Collection Air Fryer (2L) (Kitchen Appliances)",
            "Bajaj Rex 500W Mixer Grinder (3 Jars) (Kitchen Appliances)",
            "NutriBullet Pro 900W Blender (Kitchen Appliances)",
            "Instant Pot Duo Plus (7-in-1 Multi-Cooker) (Kitchen Appliances)",
            "Morphy Richards Prism Kettle (1.5L) (Kitchen Appliances)",
            "Bosch TrueMixx Pro Mixer Grinder (Kitchen Appliances)",
            "Panasonic Automatic Rice Cooker (2.2L) (Kitchen Appliances)",
            "KitchenAid Artisan Stand Mixer (5Qt) (Kitchen Appliances)",
            "Havells Insta Cook Induction Cooktop (Kitchen Appliances)",
            "Cuisinart Toaster Oven with Air Fryer (Kitchen Appliances)",
            "Philips Hue Go Portable Table Lamp (Lamps)",
            "IKEA FADO Glass Lamp (Lamps)",
            "Xiaomi Mi Smart LED Desk Lamp 1S (Lamps)",
            "Wipro Garnet Smart LED Table Lamp (Lamps)",
            "Syska SSK-TL-8605L Rechargeable Study Lamp (Lamps)",
            "Havells Swing Clip-on Lamp (Lamps)",
            "Nova Glass Pendant Ceiling Lamp (Lamps)",
            "Bajaj Softlite LED Bedside Lamp (Lamps)",
            "Oriental Designer Lantern (Handmade) (Lamps)",
            "Amazon Basics Modern Floor Lamp (Lamps)",
            "Ajanta Quartz Wall Clock (Round) (Clocks)",
            "Titan Table Clock (Brass Finish) (Clocks)",
            "Seiko Melodies in Motion Wall Clock (Clocks)",
            "Casio Digital Alarm Clock (Compact) (Clocks)",
            "Xiaomi Mi Smart Clock with Display (Clocks)",
            "IKEA STOMMA Minimalist Wall Clock (Clocks)",
            "Rhythm Wooden Pendulum Clock (Clocks)",
            "Timex Modern Desk Clock (Black) (Clocks)",
            "Braun Classic Analog Alarm Clock (Clocks)",
            "Citizen Office Desk Clock with Date (Clocks)",
            "Milton Thermosteel Flask (1L) (Water Bottles)",
            "Tupperware Aquasafe Water Bottle (1L) (Water Bottles)",
            "Nalgene Wide Mouth Tritan Bottle (1L) (Water Bottles)",
            "CamelBak Chute Mag Stainless Steel Bottle (Water Bottles)",
            "Hydro Flask Standard Mouth (32 oz) (Water Bottles)",
            "Contigo Autoseal Chill Stainless Bottle (Water Bottles)",
            "Borosil Hydra Trek Vacuum Insulated Flask (Water Bottles)",
            "Lifestraw Go Water Filter Bottle (Water Bottles)",
            "Cello H2O Water Bottle (Set of 3) (Water Bottles)",
            "Puma Aluminum Sports Bottle (Water Bottles)",
            "Dickies Unisex Professional Lab Coat (Lab Coats)",
            "Cherokee Workwear Revolution Men’s Lab Coat (Lab Coats)",
            "Landau 3-Pocket Women’s Medical Lab Coat (Lab Coats)",
            "Adar Universal Scrub Lab Coat (Lab Coats)",
            "Med Couture Signature Tailored Lab Coat (Lab Coats)",
            "Grey’s Anatomy Classic Fit Lab Coat (Lab Coats)",
            "VOGRYE Lightweight White Lab Coat (Lab Coats)",
            "Red Kap Button-Front Lab Coat (Lab Coats)",
            "Meta Labwear Student Lab Coat (Lab Coats)",
            "Natural Uniforms Kids Lab Coat (Lab Coats)",
            "3M Virtua Protective Safety Glasses (Safety Goggles)",
            "Pyramex Z87.1 Anti-Fog Safety Goggles (Safety Goggles)",
            "DEWALT DPG82-11 Clear Safety Goggles (Safety Goggles)",
            "Honeywell Uvex Ultra Spec 2001 Goggles (Safety Goggles)",
            "Bolle Tracker II Safety Glasses (Safety Goggles)",
            "MSA Perspecta Safety Goggles (Adjustable) (Safety Goggles)",
            "SAS Safety Corp Clear Anti-Fog Goggles (Safety Goggles)",
            "Radians Mirage Protective Eyewear (Safety Goggles)",
            "Uvex Stealth OTG Safety Goggles (Safety Goggles)",
            "NoCry Clear Safety Glasses (Safety Goggles)",
            "Amazon Basics Quad-Ruled Graph Notebook (Graphing Paper)",
            "Top Flight Graph Paper Pad (8.5x11 Inches) (Graphing Paper)",
            "Oxford Quad-Ruled Notebook (100 Sheets) (Graphing Paper)",
            "Rhodia Dot Grid Notepad (A5) (Graphing Paper)",
            "Five Star Graph Ruled Composition Notebook (Graphing Paper)",
            "Moleskine Softcover Grid Notebook (Graphing Paper)",
            "Arteza Premium Grid Paper Sketchpad (Graphing Paper)",
            "Alvin Engineering Graph Paper Pad (10x10 Grid) (Graphing Paper)",
            "Strathmore Hardbound Grid Journal (A4) (Graphing Paper)",
            "Mead Quad-Ruled Spiral Notebook (Graphing Paper)",
            "AmScope M150C-I Compound Microscope (Lab Equipment & Microscopes)",
            "Celestron 44341 Advanced Biological Microscope (Lab Equipment & Microscopes)",
            "OMAX Digital Binocular Compound Microscope (Lab Equipment & Microscopes)",
            "Swift SW380B Compound Lab Microscope (Lab Equipment & Microscopes)",
            "National Optical Compound Student Microscope (Lab Equipment & Microscopes)",
            "Dino-Lite Handheld Digital Microscope (Lab Equipment & Microscopes)",
            "Leica DM500 LED Biological Microscope (Lab Equipment & Microscopes)",
            "ZEISS Primo Star Microscope (Lab Equipment & Microscopes)",
            "LABOMED LX400 Binocular Microscope (Lab Equipment & Microscopes)",
            "Nikon Eclipse E200 Biological Microscope (Lab Equipment & Microscopes)",
            "\"Pride and Prejudice\" by Jane Austen (Literature Books)",
            "\"To Kill a Mockingbird\" by Harper Lee (Literature Books)",
            "\"1984\" by George Orwell (Literature Books)",
            "\"The Great Gatsby\" by F. Scott Fitzgerald (Literature Books)",
            "\"Moby Dick\" by Herman Melville (Literature Books)",
            "\"War and Peace\" by Leo Tolstoy (Literature Books)",
            "\"Jane Eyre\" by Charlotte Brontë (Literature Books)",
            "\"Wuthering Heights\" by Emily Brontë (Literature Books)",
            "\"The Catcher in the Rye\" by J.D. Salinger (Literature Books)",
            "\"Great Expectations\" by Charles Dickens (Literature Books)",
            "\"The Hunger Games\" by Suzanne Collins (Fiction Novels)",
            "\"Divergent\" by Veronica Roth (Fiction Novels)",
            "\"Harry Potter and the Sorcerer’s Stone\" by J.K. Rowling (Fiction Novels)",
            "\"The Maze Runner\" by James Dashner (Fiction Novels)",
            "\"Twilight\" by Stephenie Meyer (Fiction Novels)",
            "\"The Fault in Our Stars\" by John Green (Fiction Novels)",
            "\"The Alchemist\" by Paulo Coelho (Fiction Novels)",
            "\"Percy Jackson and the Olympians: The Lightning Thief\" by Rick Riordan (Fiction Novels)",
            "\"A Song of Ice and Fire: Game of Thrones\" by George R.R. Martin (Fiction Novels)",
            "\"Ready Player One\" by Ernest Cline (Fiction Novels)",
            "Moleskine Classic Notebook (Hardcover) (Journals)",
            "Leuchtturm1917 Bullet Journal Edition 2 (Journals)",
            "Paperage Dotted Journal (A5) (Journals)",
            "Clever Fox Planner Pro (Undated) (Journals)",
            "Scribbles That Matter Pro Journal (Journals)",
            "Archer & Olive Dot Grid Journal (A5) (Journals)",
            "Rhodia Webnotebook Hardcover Journal (Journals)",
            "Lamy Hardcover Lined Journal (Journals)",
            "Panda Planner Classic Weekly Planner (Journals)",
            "Baron Fig Confidant Notebook (Journals)",
            "Kindle Paperwhite (11th Gen) (E-Readers)",
            "Kindle Oasis (Waterproof, Adjustable Warm Light) (E-Readers)",
            "Kobo Clara HD (E-Readers)",
            "Amazon Kindle Scribe (With Note-Taking) (E-Readers)",
            "BOOX Note Air2 Plus (E-Readers)",
            "PocketBook InkPad Color (E-Readers)",
            "Onyx BOOX Nova Air C (E-Readers)",
            "Sony DPT-RP1 Digital Paper Tablet (E-Readers)",
            "Kobo Libra 2 (ComfortLight PRO) (E-Readers)",
            "Kindle Basic (10th Gen) (E-Readers)",
            "Periodic Table of Elements (Laminated Educational Poster) (Posters (For Dorm/Hostels Rooms, Study Spaces))",
            "Engineering Formulas & Equations Chart (Posters (For Dorm/Hostels Rooms, Study Spaces))",
            "ISRO & Indian Space Missions Infographic (Posters (For Dorm/Hostels Rooms, Study Spaces))",
            "Inspirational Quote Poster (${"Dream Big, Work Hard"}) (Posters (For Dorm/Hostels Rooms, Study Spaces))",
            "Tesla vs. Edison Innovation Poster (Posters (For Dorm/Hostels Rooms, Study Spaces))",
            "Blueprint of a Mechanical Gear System (Posters (For Dorm/Hostels Rooms, Study Spaces))",
            "Wright Brothers' First Flight Engineering Sketch (Posters (For Dorm/Hostels Rooms, Study Spaces))",
            "Einstein’s Theory of Relativity Equation Poster (Posters (For Dorm/Hostels Rooms, Study Spaces))",
            "Robotics & AI Concept Diagram Poster (Posters (For Dorm/Hostels Rooms, Study Spaces))",
            "Famous Indian Engineers & Scientists Tribute Poster (Posters (For Dorm/Hostels Rooms, Study Spaces))",
            "InkWynk Engineering Student Planner (Planners (For Study, Assignments, Projects))",
            "Classmate Xtra Long Study Planner (Planners (For Study, Assignments, Projects))",
            "Clever Fox Academic Planner (Planners (For Study, Assignments, Projects))",
            "Factor Notes Exam Prep Planner (Planners (For Study, Assignments, Projects))",
            "Productivity Guru - IIT-JEE Planner (Planners (For Study, Assignments, Projects))",
            "Undated Daily Planner by Factor Notes (Planners (For Study, Assignments, Projects))",
            "Luxor Engineering Journal & Planner Combo (Planners (For Study, Assignments, Projects))",
            "Rocketbook Smart Reusable Planner (Planners (For Study, Assignments, Projects))",
            "Indian Institute of Engineers Official Diary (Planners (For Study, Assignments, Projects))",
            "The Ultimate Project Management Planner (Planners (For Study, Assignments, Projects))",
            "Philips Drip Coffee Machine HD7431/20 (Coffee Makers (For Late-Night Studying & Hustle Mode))",
            "Morphy Richards Europa Espresso Maker (Coffee Makers (For Late-Night Studying & Hustle Mode))",
            "Nescafe É Smart Coffee Maker & Mug (Coffee Makers (For Late-Night Studying & Hustle Mode))",
            "Preethi Drip Café Coffee Maker (Coffee Makers (For Late-Night Studying & Hustle Mode))",
            "InstaCuppa French Press Coffee Maker (Coffee Makers (For Late-Night Studying & Hustle Mode))",
            "Hario V60 Ceramic Coffee Dripper (Coffee Makers (For Late-Night Studying & Hustle Mode))",
            "Black+Decker DCM25 Personal Coffee Brewer (Coffee Makers (For Late-Night Studying & Hustle Mode))",
            "Havells Donato Espresso Coffee Machine (Coffee Makers (For Late-Night Studying & Hustle Mode))",
            "Delonghi Dedica EC685 Coffee Maker (Coffee Makers (For Late-Night Studying & Hustle Mode))",
            "Pigeon Brewster Drip Coffee Maker (Coffee Makers (For Late-Night Studying & Hustle Mode))",
            "Bombay Dyeing Blackout Curtains (Curtains)",
            "D’Decor Anti-Dust Room Darkening Curtains (Curtains)",
            "Home Sizzler Polyester Window Curtains (Curtains)",
            "AmazonBasics Light-Blocking Curtains (Curtains)",
            "Spaces Premium Cotton Blend Curtains (Curtains)",
            "IKEA MERETE Noise-Reducing Curtains (Curtains)",
            "Luxor Uniplane Thermal Insulated Curtains (Curtains)",
            "CasaCraft Sheer Printed Engineering-Themed Curtains (Curtains)",
            "SleepyCat Soundproof Dorm Room Curtains (Curtains)",
            "Story@Home UV-Protective Hostel Curtains (Curtains)",
            "Bombay Dyeing Pure Cotton Bedsheets (Bed Sheets)",
            "Jaipur Fabric Printed Single Bed Sheet (Bed Sheets)",
            "Swayam Microfiber Hostel Bed Set (Bed Sheets)",
            "Wakefit SoftTouch Bedsheets (Bed Sheets)",
            "Urban Space Engineering Sketch Printed Bedsheet (Bed Sheets)",
            "Spaces Cotton Striped Hostel Bedsheet (Bed Sheets)",
            "Amazon Solimo Hypoallergenic Bedsheet (Bed Sheets)",
            "Home Centre Egyptian Cotton Bedsheet (Bed Sheets)",
            "Raymond Premium Dorm Bedding Set (Bed Sheets)",
            "FabIndia Organic Cotton Hostel Bedsheet (Bed Sheets)",
            "SleepyCat Memory Foam Pillow (Pillows)",
            "Wakefit Orthopedic Pillow (Pillows)",
            "The White Willow Contour Cervical Pillow (Pillows)",
            "Kurlon Hostel Pillow (Budget-Friendly) (Pillows)",
            "DreamyHome Anti-Allergic Cotton Pillow (Pillows)",
            "AmazonBasics Ultra-Soft Microfiber Pillow (Pillows)",
            "Flo Ergo Adjustable Loft Pillow (Pillows)",
            "Hush Hybrid Gel Infused Memory Pillow (Pillows)",
            "Dr. Trust Smart Cool Gel Pillow (Pillows)",
            "Springfit SoftCloud Pillow (Pillows)",
            "IKEA VINDUM Plush Rug (Rugs)",
            "D’Decor Anti-Slip Floor Rug (Rugs)",
            "Urban Space Modern Abstract Rug (Rugs)",
            "Jaipur Rugs Handmade Wool Carpet (Rugs)",
            "Status Home Geometric Printed Rug (Rugs)",
            "Story@Home Soft Polyester Shaggy Rug (Rugs)",
            "Pepperfry Abstract Dorm Carpet (Rugs)",
            "DreamWeavers Hostel Room Mat (Rugs)",
            "Saral Home Anti-Skid Bedside Rug (Rugs)",
            "Bombay Dyeing Cotton Dorm Floor Rug (Rugs)",
            "Havells Swing Wall Fan (Fans)",
            "Bajaj Esteem Table Fan (Fans)",
            "Orient Electric Smart Air Cooler (Fans)",
            "Usha Maxx Air Tower Fan (Fans)",
            "Crompton High-Speed Ceiling Fan (Fans)",
            "Atomberg Efficio BLDC Smart Fan (Fans)",
            "Luminous Dhoom High-Speed Ceiling Fan (Fans)",
            "Havells Ciera Remote-Control Pedestal Fan (Fans)",
            "Orient Electric Aeroquiet Premium Fan (Fans)",
            "V-Guard Esfera Rechargeable Table Fan (Fans)",
            "Popy Black Unisex Windproof Umbrella (Fans)",
            "John’s UV-Protected Automatic Umbrella (Fans)",
            "Sun UV Silver-Coated Folding Umbrella (Fans)",
            "Aristocrat Strong Windproof Umbrella (Fans)",
            "Totes Auto-Open Engineering Umbrella (Fans)",
            "Decathlon Compact Travel Umbrella (Fans)",
            "American Tourister Water-Repellent Umbrella (Fans)",
            "FabSeasons Reversible Umbrella (Fans)",
            "AmazonBasics Golf Umbrella (Extra Large) (Fans)",
            "Quechua Trekking Umbrella (For Outdoor Lab Work) (Fans)",
            "Wildcraft HypaDry Rain Jacket (Umbrellas)",
            "Duckback PVC Waterproof Raincoat (Umbrellas)",
            "Quechua Trekking Rain Poncho (Umbrellas)",
            "AmazonBasics Reusable Rain Poncho (Umbrellas)",
            "Zeel Printed Hooded Raincoat (Umbrellas)",
            "Duckback Classic Two-Piece Rain Suit (Umbrellas)",
            "FabSeasons Unisex Waterproof Coat (Umbrellas)",
            "HRX Sports Rain Windcheater (Umbrellas)",
            "Columbia Glennaker Lake Rain Jacket (Umbrellas)",
            "Aristocrat Bike Rider’s Rainwear (Umbrellas)",
            "Sparx SM-482 College Sneakers (Raincoats)",
            "Campus OXYFIT Walking Shoes (Raincoats)",
            "Adidas Duramo SL Running Shoes (Raincoats)",
            "Puma Unisex IDP Sneakers (Raincoats)",
            "Bata Power Athletic Shoes (Raincoats)",
            "Woodland GB 1204115 Outdoor Shoes (Raincoats)",
            "Decathlon Artengo Tennis Shoes (Raincoats)",
            "Liberty Gliders Slip-On Shoes (Raincoats)",
            "Lancer Air-Breeze Sports Shoes (Raincoats)",
            "Relaxo Bahamas Flip-Flops (Hostel Wear) (Raincoats)",
            "Decathlon Kipsta Quick-Dry Football Jersey (Shoes)",
            "Nike Dry-Fit Training Jersey (Shoes)",
            "Adidas Performance Team Jersey (Shoes)",
            "Shiv Naresh Cricket Team Jersey (Shoes)",
            "Puma Men’s Graphic Training Tee (Shoes)",
            "HRX by Hrithik Roshan Gym Jersey (Shoes)",
            "Cosco Sleeveless Basketball Jersey (Shoes)",
            "Nivia Polyester Gym T-Shirt (Shoes)",
            "Under Armour Tech Sports Jersey (Shoes)",
            "Local Custom-Made College Team Jerseys (Shoes)",
            "Kore PVC Dumbbell Set (10kg) (Sports Jerseys)",
            "Cockatoo Hex Dumbbell Pair (Sports Jerseys)",
            "Decathlon Domyos Adjustable Dumbbells (Sports Jerseys)",
            "Strauss Neoprene Dumbbells (2kg) (Sports Jerseys)",
            "Flexnest Adjustable Smart Dumbbell (Sports Jerseys)",
            "RubX Rubber-Coated Dumbbells (5kg) (Sports Jerseys)",
            "AmazonBasics Cast Iron Dumbbell Set (Sports Jerseys)",
            "Nivia Hex Dumbbells (Gym Use) (Sports Jerseys)",
            "USI Universal Fixed Weight Dumbbells (Sports Jerseys)",
            "Aurion Vinyl Dumbbell Set (Home Gym) (Sports Jerseys)",
            "Decathlon Domyos Essential Yoga Mat (Dumbbells)",
            "AmazonBasics Thick Foam Mat (Dumbbells)",
            "Strauss Anti-Slip Yoga Mat (Dumbbells)",
            "Kobo TPE Eco-Friendly Mat (Dumbbells)",
            "Boldfit Yoga Mat for Home Workout (Dumbbells)",
            "Nivia Soft Cushion Yoga Mat (Dumbbells)",
            "Wakefit Extra Thick Meditation Mat (Dumbbells)",
            "HRX Grip Flow Yoga Mat (Dumbbells)",
            "FEGSY Foldable Yoga Mat (Dumbbells)",
            "Reebok Studio Yoga Mat (Dumbbells)",
            "Vega Crux Flip-Up Helmet (Yoga Mats)",
            "Steelbird SBA-2 Full Face Helmet (Yoga Mats)",
            "Studds Thunder Open Face Helmet (Yoga Mats)",
            "Royal Enfield Street Prime Helmet (Yoga Mats)",
            "Axor Apex Venomous Dual Visor Helmet (Yoga Mats)",
            "LS2 Rapid Street Bike Helmet (Yoga Mats)",
            "TVS Race X Smart Helmet (Yoga Mats)",
            "Ignyte IGN-4 Moto Helmet (Yoga Mats)",
            "SMK Twister Full-Face Helmet (Yoga Mats)",
            "Wrangler Classic Rider Helmet (Yoga Mats)",
            "Strauss Cruiser Skateboard (Helmets)",
            "Jaspo Hurricane 27-Inch Skateboard (Helmets)",
            "Nivia Maplewood Skateboard (Helmets)",
            "Decathlon Oxelo Beginner Skateboard (Helmets)",
            "RazorX Electric Skateboard (Helmets)",
            "Kryptonics Super Cruiser Longboard (Helmets)",
            "Cosco Street Skater Board (Helmets)",
            "Winklevoss Mini Skateboard (Helmets)",
            "Teamgee H5 Electric Skateboard (Helmets)",
            "Razor RipStik Caster Board (Helmets)",
            "Maybelline Fit Me Basic Makeup Kit (Skateboards)",
            "Lakmé College Essentials Makeup Set (Skateboards)",
            "Blue Heaven Budget Makeup Combo (Skateboards)",
            "Sugar Cosmetics College Starter Kit (Skateboards)",
            "Elle 18 Beauty Kit (Affordable) (Skateboards)",
            "Swiss Beauty Everyday Makeup Kit (Skateboards)",
            "Mamaearth Natural Glow Kit (Skateboards)",
            "Faces Canada Student-Friendly Makeup Kit (Skateboards)",
            "Coloressence Quick Touch-Up Kit (Skateboards)",
            "Miss Claire Professional Kit (Skateboards)",
            "Philips Essential Hair Dryer HP8100 (Makeup Kits)",
            "Havells Foldable Hair Dryer (Makeup Kits)",
            "Nova NHP-8100 Budget Hair Dryer (Makeup Kits)",
            "Wahl Super Dry Professional Dryer (Makeup Kits)",
            "Vega Go-Style Compact Hair Dryer (Makeup Kits)",
            "Remington D5000 Compact Dryer (Makeup Kits)",
            "SYSKA Hair Dryer HD1605 (Makeup Kits)",
            "CHAOBA 2000W Hair Blower (Makeup Kits)",
            "Braun Satin Hair 1 Dryer (Makeup Kits)",
            "Rozia Hair Styling Dryer (Makeup Kits)",
            "Philips Selfie Straightener HP8302 (Hair Dryers)",
            "Havells HS4101 Hair Straightener (Hair Dryers)",
            "Vega Shine Styling Straightener (Hair Dryers)",
            "Nova Ceramic Hair Straightener (Hair Dryers)",
            "SYSKA HS6810 Hair Styler (Hair Dryers)",
            "Kemei KM-329 Professional Straightener (Hair Dryers)",
            "Remington S3500 Ceramic Straightener (Hair Dryers)",
            "Wahl Cutek Hair Straightener (Hair Dryers)",
            "Rozia Flat Iron Hair Straightener (Hair Dryers)",
            "Beurer HS80 Advanced Straightener (Hair Dryers)",
            "WildHorn Genuine Leather Wallet (Hair Straighteners)",
            "Urban Forest RFID Blocking Wallet (Hair Straighteners)",
            "Tommy Hilfiger College Wallet (Hair Straighteners)",
            "Puma Polyester Slim Wallet (Hair Straighteners)",
            "Fastrack Men’s Synthetic Wallet (Hair Straighteners)",
            "American Tourister Minimalist Wallet (Hair Straighteners)",
            "Hidesign Classic Leather Wallet (Hair Straighteners)",
            "Allen Solly Casual Bi-Fold Wallet (Hair Straighteners)",
            "Swiss Military Water-Resistant Wallet (Hair Straighteners)",
            "Titan RFID Secure Wallet (Hair Straighteners)",
            "Fastrack Reflex Beat+ Smartwatch (Wallets)",
            "Noise ColorFit Pulse 2 Max (Wallets)",
            "Fire-Boltt Ninja Call Smartwatch (Wallets)",
            "Casio Youth Series Digital Watch (Wallets)",
            "Sonata Super Fibre Digital Watch (Wallets)",
            "Timex Expedition Analog-Digital (Wallets)",
            "Xiaomi Mi Smart Band 6 (Wallets)",
            "Boat Xtend Smartwatch (Wallets)",
            "Amazfit Bip U Pro (Wallets)",
            "Titan Neo Economy Series (Wallets)",
            "Fastrack UV-Protected Sunglasses (Watches)",
            "Ray-Ban Aviator Classic (Watches)",
            "Peter Jones Budget-Friendly Sunglasses (Watches)",
            "Polaroid Polarized Shades (Watches)",
            "Titan Full-Rim Sports Sunglasses (Watches)",
            "Vincent Chase Blue Light Protection (Watches)",
            "Oakley Holbrook Sports Sunglasses (Watches)",
            "Carrera Oversized Fashion Shades (Watches)",
            "Decathlon Cycling & Running Glasses (Watches)",
            "IDEE Unisex Square Sunglasses (Watches)",
            "Yellow Chimes Stainless Steel Chain (Sunglasses)",
            "Voylla Classic Oxidized Earrings (Sunglasses)",
            "Zaveri Pearls College Fest Jewelry (Sunglasses)",
            "Shining Diva Minimal Rings (Sunglasses)",
            "Swarovski Crystal Pendant (Sunglasses)",
            "Titan Raga Women’s Bracelet (Sunglasses)",
            "Fastrack Streetwear Leather Bracelet (Sunglasses)",
            "CaratLane Silver Nose Pin (Sunglasses)",
            "Pipa Bella Layered Necklace (Sunglasses)",
            "Daniel Wellington Rose Gold Chain (Sunglasses)",
            "Eveready LED Rechargeable Flashlight (Jewelry)",
            "Wipro Torch with Adjustable Beam (Jewelry)",
            "iBELL Tactical Military Flashlight (Jewelry)",
            "AmazonBasics Emergency Flashlight (Jewelry)",
            "Philips LED Torchlight (Jewelry)",
            "SYSKA Dynamo Wind-Up Flashlight (Jewelry)",
            "Decathlon Trekking Flashlight (Jewelry)",
            "Black & Decker Heavy-Duty Torch (Jewelry)",
            "Brightlite Ultra Beam Torch (Jewelry)",
            "Havells Emergency Handheld Flashlight (Jewelry)",
            "Bosch GSB 500W Home Tool Kit (Flashlights)",
            "Black & Decker 109-Piece Hand Tool Set (Flashlights)",
            "Taparia Universal Tool Kit (Flashlights)",
            "Stanley 46-Piece Tool Set (Flashlights)",
            "INGCO Multi-Purpose Tool Kit (Flashlights)",
            "AmazonBasics Household Repair Kit (Flashlights)",
            "TATA Agrico Basic Tool Kit (Flashlights)",
            "Wulf Claw Hammer & Screwdriver Set (Flashlights)",
            "Spartan Electrical Tool Set (Flashlights)",
            "Honda Two-Wheeler Repair Toolkit (Flashlights)",
            "Taparia 840 Screwdriver Set (Toolkits)",
            "Bosch Go Cordless Screwdriver (Toolkits)",
            "Stanley 10-Piece Precision Set (Toolkits)",
            "JK Vision Multipurpose Screwdriver (Toolkits)",
            "iBELL Electric Screwdriver (Toolkits)",
            "Visko 6-in-1 Multi-Tool (Toolkits)",
            "Black+Decker Compact Screwdriver (Toolkits)",
            "Spartan Magnetic Screwdriver Kit (Toolkits)",
            "INGCO Electric Screwdriver (Toolkits)",
            "Foster Heavy-Duty Screwdriver Set (Toolkits)",
            "Havells 4-Socket Power Strip (Screwdrivers)",
            "GM Modular Extension Board (Screwdrivers)",
            "Syska Power Track Surge Protector (Screwdrivers)",
            "Philips 6-Port Extension Cord (Screwdrivers)",
            "Bajaj Heavy-Duty Power Strip (Screwdrivers)",
            "AmazonBasics Multi-Plug Extension Cord (Screwdrivers)",
            "Portronics Power Plate 5 (Screwdrivers)",
            "Goldmedal 360° Rotatable Plug (Screwdrivers)",
            "Anchor Spike Guard Extension Board (Screwdrivers)",
            "Wipro 3-Meter Extension Strip (Screwdrivers)",
            "Wipro Garnet LED Table Lamp (Extension Cords)",
            "Philips Smart Desk Lamp (Extension Cords)",
            "IKEA Forså Adjustable Lamp (Extension Cords)",
            "AmazonBasics Rechargeable Study Lamp (Extension Cords)",
            "SYSKA LED Desk Lamp (Extension Cords)",
            "Pigeon LED Touch Control Lamp (Extension Cords)",
            "Orient Electric Bright Desk Light (Extension Cords)",
            "Mi Smart LED Desk Lamp (Extension Cords)",
            "Bajaj LED Flexible Table Lamp (Extension Cords)",
            "Havells Color-Changing Desk Lamp (Extension Cords)",
            "AmazonBasics Cat-6 Ethernet Cable (Study Lamps)",
            "D-Link RJ45 Network Cable (Study Lamps)",
            "TP-Link High-Speed LAN Cable (Study Lamps)",
            "Zebronics 5m Ethernet Cable (Study Lamps)",
            "Quantum Ultra-Fast Internet Cable (Study Lamps)",
            "Tizum Braided Network Cable (Study Lamps)",
            "Digisol Gold-Plated Ethernet Wire (Study Lamps)",
            "Cadyce Flat LAN Cable (Study Lamps)",
            "Belkin Premium Ethernet Cord (Study Lamps)",
            "Foxin High-Speed Internet Wire (Study Lamps)",
            "AmazonBasics Cat-6 Ethernet Cable (Ethernet Cables)",
            "D-Link RJ45 Network Cable (Ethernet Cables)",
            "TP-Link High-Speed LAN Cable (Ethernet Cables)",
            "Zebronics 5m Ethernet Cable (Ethernet Cables)",
            "Quantum Ultra-Fast Internet Cable (Ethernet Cables)",
            "Tizum Braided Network Cable (Ethernet Cables)",
            "Digisol Gold-Plated Ethernet Wire (Ethernet Cables)",
            "Cadyce Flat LAN Cable (Ethernet Cables)",
            "Belkin Premium Ethernet Cord (Ethernet Cables)",
            "Foxin High-Speed Internet Wire (Ethernet Cables)",
            "AmazonBasics High-Speed HDMI Cable (USB)",
            "UGREEN 4K HDMI Cord (USB)",
            "Portronics Vivid HDMI Cable (USB)",
            "Tizum Braided 4K HDMI Wire (USB)",
            "Belkin Ultra HD HDMI Connector (USB)",
            "Boat 8K HDMI Gold-Plated Cable (USB)",
            "Croma Heavy-Duty HDMI Wire (USB)",
            "Foxin Universal HDMI Connector (USB)",
            "Quantum Premium HDMI Cable (USB)",
            "Zebronics HDMI High-Performance Cable (USB)",
            "Boat Dual-Audio Splitter (Headphone Splitters)",
            "AmazonBasics 3.5mm Audio Splitter (Headphone Splitters)",
            "UGREEN 2-in-1 Audio Jack (Headphone Splitters)",
            "Portronics Sound Splitter (Headphone Splitters)",
            "PTron Y-Shaped Headphone Splitter (Headphone Splitters)",
            "Quantum Dual-Port Audio Adapter (Headphone Splitters)",
            "Belkin Multi-Audio Share Jack (Headphone Splitters)",
            "Croma High-Quality Splitter (Headphone Splitters)",
            "Philips 3-Way Audio Sharing Cable (Headphone Splitters)",
            "Sony Noise-Free Headphone Splitter (Headphone Splitters)",
            "IKEA Billy Compact Bookshelf (Bookshelves)",
            "AmazonBasics 3-Tier Wooden Shelf (Bookshelves)",
            "Urban Ladder Particle Board Shelf (Bookshelves)",
            "Wakefit Engineered Wood Bookshelf (Bookshelves)",
            "Nilkamal Plastic 4-Shelf Organizer (Bookshelves)",
            "Godrej Metal Bookshelf (Bookshelves)",
            "HomeTown Compact Wall-Mount Shelf (Bookshelves)",
            "Spacewood Modular Book Rack (Bookshelves)",
            "Supreme Plastic Mini Bookshelf (Bookshelves)",
            "Flipkart SmartBuy Multipurpose Rack (Bookshelves)",
            "Writop Magnetic Whiteboard (Whiteboards)",
            "Hauser Double-Sided Writing Board (Whiteboards)",
            "Glassboard Frameless Study Board (Whiteboards)",
            "Classmate Portable Whiteboard (Whiteboards)",
            "AmazonBasics Foldable Study Board (Whiteboards)",
            "Nechams Dry-Erase Memo Board (Whiteboards)",
            "IKEA LÄTT Wall Whiteboard (Whiteboards)",
            "Wipro Smart Writing Board (Whiteboards)",
            "Solo Standing Whiteboard (Whiteboards)",
            "Flipkart Office Essentials Whiteboard (Whiteboards)",
            "Cello Whitemate Marker Set (Markers)",
            "Faber-Castell Dry-Erase Markers (Markers)",
            "Luxor Bullet Tip Whiteboard Markers (Markers)",
            "Staedtler Lumocolor Erasable Pens (Markers)",
            "AmazonBasics Assorted Color Markers (Markers)",
            "Camlin Refillable Board Markers (Markers)",
            "Reynolds Non-Toxic Markers (Markers)",
            "Pilot Twin-Tip Dry Markers (Markers)",
            "Uni-Ball Power Board Markers (Markers)",
            "Sharpie Ultra-Fine Tip Markers (Markers)",
            "Solo Sturdy Clipboard (Clipboards)",
            "AmazonBasics Wooden Clip Holder (Clipboards)",
            "Faber-Castell Premium Clipboard (Clipboards)",
            "Oddy Transparent A4 Clipboard (Clipboards)",
            "Classmate Foldable Writing Pad (Clipboards)",
            "Navneet Exam Clip Board (Clipboards)",
            "Camlin Metal Clip Board (Clipboards)",
            "StatMo Waterproof Writing Board (Clipboards)",
            "OfficeMate Multi-Color Clipboard (Clipboards)",
            "Kangaro Smart Clip Holder (Clipboards)",
            "HRX by Hrithik Roshan Classic Tee (T-Shirts)",
            "Roadster Oversized College T-Shirt (T-Shirts)",
            "Levi’s Printed Cotton Tee (T-Shirts)",
            "Bewakoof Graphic T-Shirts (T-Shirts)",
            "Puma Dry-Fit Sports Tee (T-Shirts)",
            "Allen Solly Polo T-Shirt (T-Shirts)",
            "Adidas Campus Casual Tee (T-Shirts)",
            "Uniqlo Solid Color T-Shirts (T-Shirts)",
            "US Polo Assn. Casual Cotton Tee (T-Shirts)",
            "Decathlon Quechua Trekking T-Shirt (T-Shirts)",
            "Nike Zip-Up Hoodie (Hoodies)",
            "Puma Classic Fleece Hoodie (Hoodies)",
            "Campus Sutra Printed Hoodie (Hoodies)",
            "Roadster Winter Hoodie (Hoodies)",
            "Jack & Jones Pullover Hoodie (Hoodies)",
            "HRX Athleisure Hoodie (Hoodies)",
            "Levi’s Cotton Sweatshirt (Hoodies)",
            "Monte Carlo Woolen Hoodie (Hoodies)",
            "US Polo Assn. Full Sleeve Hoodie (Hoodies)",
            "Adidas Essentials Hoodie (Hoodies)",
            "Levi’s 511 Slim Fit Jeans (Jeans)",
            "Roadster Stretchable Denim (Jeans)",
            "Flying Machine Classic Fit Jeans (Jeans)",
            "Pepe Jeans Tapered Fit (Jeans)",
            "Spykar High-Rise Denim (Jeans)",
            "Wrangler Regular Fit Jeans (Jeans)",
            "Lee Stretchable Casual Jeans (Jeans)",
            "Van Heusen Formal Denim (Jeans)",
            "Killer Slim Tapered Jeans (Jeans)",
            "Allen Solly Comfortable Jeans (Jeans)",
            "FabIndia Cotton Midi Dress (Dresses)",
            "AND A-Line Casual Dress (Dresses)",
            "Biba Printed Ethnic Dress (Dresses)",
            "Global Desi Boho Maxi Dress (Dresses)",
            "Myntra Roadster Denim Dress (Dresses)",
            "Zara Floral Fit & Flare Dress (Dresses)",
            "Allen Solly Smart Casual Dress (Dresses)",
            "Forever 21 Bodycon Party Dress (Dresses)",
            "Pantaloons Ruffle Sleeve Dress (Dresses)",
            "H&M Basic T-Shirt Dress (Dresses)",
            "Van Heusen Blazer Coat (Coats)",
            "H&M Classic Trench Coat (Coats)",
            "Monte Carlo Woolen Overcoat (Coats)",
            "Decathlon Quechua Padded Jacket (Coats)",
            "Roadster Faux Leather Coat (Coats)",
            "Peter England Single-Breasted Coat (Coats)",
            "US Polo Assn. Woolen Coat (Coats)",
            "Levi’s Denim Jacket (Coats)",
            "Blackberrys Tailored Overcoat (Coats)",
            "Allen Solly Business Casual Coat (Coats)",
            "Decathlon Quechua Thermal Gloves (Gloves)",
            "Adidas Training Gym Gloves (Gloves)",
            "Woodland Woolen Hand Gloves (Gloves)",
            "3M Industrial Safety Gloves (Gloves)",
            "Puma Half-Finger Workout Gloves (Gloves)",
            "FabSeasons Knitted Winter Gloves (Gloves)",
            "Dr. Odin Latex Medical Gloves (Gloves)",
            "Reebok Heavy-Duty Gym Gloves (Gloves)",
            "Wildcraft Trekking Gloves (Gloves)",
            "Ansell Chemical Resistant Gloves (Gloves)",
            "Nike Baseball Cap (Caps)",
            "Adidas Running Cap (Caps)",
            "Decathlon Hiking Sun Cap (Caps)",
            "Puma Adjustable Snapback Cap (Caps)",
            "HRX Classic Black Cap (Caps)",
            "Woodland Outdoor Sports Cap (Caps)",
            "Levi’s Denim Cap (Caps)",
            "H&M Bucket Hat (Caps)",
            "Jack & Jones Printed Cap (Caps)",
            "New Era Sports Cap (Caps)",
            "Bombay Dyeing Cozy Blanket (Blankets)",
            "AmazonBasics Microfiber Blanket (Blankets)",
            "Solimo Reversible Comforter (Blankets)",
            "Sleepwell Soft Fleece Blanket (Blankets)",
            "Duroflex Warm Winter Blanket (Blankets)",
            "Raymond Home Woolen Blanket (Blankets)",
            "Cloudtail Lightweight Travel Blanket (Blankets)",
            "Urban Ladder Cotton Bedspread (Blankets)",
            "Wakefit Ultra Warm Comforter (Blankets)",
            "FabIndia Handmade Quilt (Blankets)",
            "Clay Craft Ceramic Mug (Crockery)",
            "Cello Unbreakable Dinner Set (Crockery)",
            "Milton Microwave-Safe Bowl Set (Crockery)",
            "Borosil Glass Dinner Plates (Crockery)",
            "La Opala Elegant Dining Set (Crockery)",
            "AmazonBasics Stainless Steel Cutlery (Crockery)",
            "Corelle Lightweight Dinner Set (Crockery)",
            "Treo by Milton Coffee Mug Set (Crockery)",
            "Prestige Insulated Lunch Bowls (Crockery)",
            "Wonderchef Porcelain Tea Cups (Crockery)",
            "Kent Gold + Gravity Water Filter (Water Filters)",
            "Tata Swach Non-Electric Water Purifier (Water Filters)",
            "Aquaguard On-The-Go Bottle Filter (Water Filters)",
            "Livpure Smart Copper Water Purifier (Water Filters)",
            "Prestige Tattva Copper Water Filter (Water Filters)",
            "HUL Pureit Germkill Filter Kit (Water Filters)",
            "Blue Star Portable RO Purifier (Water Filters)",
            "ZeroB Hydrolife Gravity Purifier (Water Filters)",
            "Eureka Forbes AquaSure Filter (Water Filters)",
            "Milton Copper Water Purifier (Water Filters)",
            "AmazonBasics Foldable Storage Box (Storage Boxes)",
            "Nilkamal Plastic Organizer Box (Storage Boxes)",
            "IKEA Skubb Underbed Storage Box (Storage Boxes)",
            "Cello Stackable Storage Container (Storage Boxes)",
            "Home Centre Multipurpose Box (Storage Boxes)",
            "Tupperware Smart Storage Bin (Storage Boxes)",
            "Urban Ladder Wooden Organizer (Storage Boxes)",
            "Supreme Collapsible Storage Box (Storage Boxes)",
            "Flipkart SmartBuy Fabric Storage Bag (Storage Boxes)",
            "Milton Transparent Modular Box (Storage Boxes)",
            "Tupperware Lunch Box Set (Tupperware)",
            "Cello Airtight Food Containers (Tupperware)",
            "Borosil Microwave-Safe Storage Boxes (Tupperware)",
            "Milton Insulated Tiffin Box (Tupperware)",
            "Treo Glass Storage Jars (Tupperware)",
            "Solimo BPA-Free Food Storage Set (Tupperware)",
            "Prestige Clip-Lock Storage Containers (Tupperware)",
            "Home Centre Leakproof Lunch Box (Tupperware)",
            "Signoraware Unbreakable Tiffin (Tupperware)",
            "Wonderchef Steel Lunch Containers (Tupperware)",
            "Bajaj Table Fan (Fans)",
            "Usha High-Speed Pedestal Fan (Fans)",
            "Orient Electric Wall-Mount Fan (Fans)",
            "Havells Rechargeable Mini Fan (Fans)",
            "Crompton Silent Ceiling Fan (Fans)",
            "Atomberg Energy-Saving Fan (Fans)",
            "V-Guard Portable Desk Fan (Fans)",
            "Luminous Tower Fan (Fans)",
            "Panasonic Clip-On Fan (Fans)",
            "Symphony Personal Air Cooler (Fans)",
            "AmazonBasics 6-Socket Power Strip (Power Strips)",
            "GM Modular Spike Guard (Power Strips)",
            "Philips 5-Port Surge Protector (Power Strips)",
            "Belkin Multi-Plug Extension Board (Power Strips)",
            "Bajaj Heavy-Duty Power Strip (Power Strips)",
            "Wipro Smart Plug Extension (Power Strips)",
            "Portronics Power Plate 7 (Power Strips)",
            "Anchor 8-Socket Power Hub (Power Strips)",
            "Zebronics Multi-Device Charging Strip (Power Strips)",
            "Quantum USB-Powered Extension (Power Strips)",
            "Amazon Echo Dot (Alexa) (Smart Speakers)",
            "Google Nest Mini (Smart Speakers)",
            "Mi Smart Speaker (Smart Speakers)",
            "JBL Link Portable Speaker (Smart Speakers)",
            "Bose Home Speaker 300 (Smart Speakers)",
            "Apple HomePod Mini (Smart Speakers)",
            "Sony SRS Smart Wireless Speaker (Smart Speakers)",
            "Harman Kardon Aura Studio (Smart Speakers)",
            "Marshall Uxbridge Bluetooth Speaker (Smart Speakers)",
            "Realme Smart AI Speaker (Smart Speakers)",
            "Oculus Quest 2 VR Headset (VR Headsets)",
            "Sony PlayStation VR (VR Headsets)",
            "HTC Vive Virtual Reality Headset (VR Headsets)",
            "Samsung Gear VR (VR Headsets)",
            "Pimax 5K Plus (VR Headsets)",
            "Oculus Rift S (VR Headsets)",
            "Google Cardboard VR (VR Headsets)",
            "Lenovo Mirage Solo VR (VR Headsets)",
            "Realme VR Glasses (VR Headsets)",
            "ANTVR Headset (VR Headsets)",
            "GoPro Hero 10 (Action Cameras)",
            "DJI Osmo Action Camera (Action Cameras)",
            "Insta360 One R (Action Cameras)",
            "Sony FDR-X3000 (Action Cameras)",
            "SJCAM SJ8 Pro (Action Cameras)",
            "Yi 4K Action Camera (Action Cameras)",
            "Akaso Brave 7 (Action Cameras)",
            "Campark V40 (Action Cameras)",
            "Kodak PIXPRO SP360 (Action Cameras)",
            "Noise Play Vlog Camera (Action Cameras)",
            "Solo 2-Ring Binder (Binders)",
            "Classmate Spiral Binder (Binders)",
            "Faber-Castell Premium Binder (Binders)",
            "Kangaro Lever Arch Binder (Binders)",
            "Hauser Hard Cover Binder (Binders)",
            "AmazonBasics A4 Clip Binder (Binders)",
            "StatMo PVC File Binder (Binders)",
            "Oddy Expanding Document Binder (Binders)",
            "OfficeMate Transparent Binder (Binders)",
            "Navneet Multipurpose Folder (Binders)",
            "Staedtler Textsurfer Classic (Highlighters)",
            "Faber-Castell Assorted Highlighters (Highlighters)",
            "Camlin Fluorescent Highlighter Pack (Highlighters)",
            "Sharpie Smudge-Free Highlighter (Highlighters)",
            "Luxor Bright Color Set (Highlighters)",
            "Pilot FriXion Erasable Highlighters (Highlighters)",
            "AmazonBasics Multipack Highlighters (Highlighters)",
            "Uni-Ball Highlighter Pens (Highlighters)",
            "Bic Grip Highlight Marker (Highlighters)",
            "Reynolds Transparent Neon Highlighter (Highlighters)",
            "Canon LiDE 300 Scanner (Scanners)",
            "HP ScanJet Pro 2500 (Scanners)",
            "Epson Perfection V39 (Scanners)",
            "Brother ADS-1700W (Scanners)",
            "Fujitsu ScanSnap iX500 (Scanners)",
            "Kodak Alaris Desktop Scanner (Scanners)",
            "Plustek OpticSlim 1180 (Scanners)",
            "Portronics Portable Scanner (Scanners)",
            "Xiaomi Mi Smart Scanner (Scanners)",
            "Flipkart SmartBuy Flatbed Scanner (Scanners)",
            "Digitek Lightweight Tripod (Tripods)",
            "AmazonBasics 60-Inch Tripod (Tripods)",
            "Manfrotto Compact Action Tripod (Tripods)",
            "Benro Adjustable Tripod (Tripods)",
            "Joby GorillaPod Flexible Tripod (Tripods)",
            "Sony VCT-R640 Tripod (Tripods)",
            "Ulanzi Travel Tripod (Tripods)",
            "Kodak T210 Portable Tripod (Tripods)",
            "Regetek Universal Tripod (Tripods)",
            "Simpex 360° Swivel Tripod (Tripods)",
            "Logitech G240 Gaming Mousepad (Mousepads)",
            "Redgear MP80 Extended Mousepad (Mousepads)",
            "Razer Goliathus Speed Edition (Mousepads)",
            "SteelSeries QcK Mini Mousepad (Mousepads)",
            "AmazonBasics Standard Mousepad (Mousepads)",
            "Cosmic Byte HyperGiant XL Mousepad (Mousepads)",
            "Lenovo Legion Control Mousepad (Mousepads)",
            "Corsair MM200 Cloth Mousepad (Mousepads)",
            "HP OMEN Gaming Mousepad (Mousepads)",
            "Zebronics Heavy-Duty Mousepad (Mousepads)",
            "Mi Smart Band 7 (Fitness Bands)",
            "Realme Band 2 (Fitness Bands)",
            "OnePlus Smart Band (Fitness Bands)",
            "Noise ColorFit Pulse (Fitness Bands)",
            "Honor Band 6 (Fitness Bands)",
            "Fitbit Charge 5 (Fitness Bands)",
            "Fastrack Reflex 3.0 (Fitness Bands)",
            "Samsung Galaxy Fit 2 (Fitness Bands)",
            "Boat Xtend Smart Band (Fitness Bands)"
        )

        var adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_dropdown_item_1line,
            productListAutoComplete
        )
        binding!!.editTextProductName.setAdapter(adapter)


// Firebase Storage reference


// Download file from Firebase Storage


    }

    private fun logicForLoadingLottieIfUserEnterNoPreDefinedInputs(enteredText: String) {
        var text: String = enteredText.lowercase()
        text = text.replace("\\s".toRegex(), "")
//        Toast.makeText(requireContext(), "SECONDARY : $text", Toast.LENGTH_SHORT).show()

        if (listOf(
                "smartphone",
                "mobile",
                "android",
                "iphone",
                "samsung",
                "redmi",
                "oneplus",
                "oppo",
                "vivo",
                "pixel",
                "realme",
                "asus",
                "nokia",
                "huawei",
                "motorola"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("smartphones")
        } else if (listOf(
                "fashionwatch",
                "watch",
                "rolex",
                "casio",
                "fossil",
                "titan",
                "applewatch",
                "samsungwatch",
                "seiko",
                "timex",
                "citizen",
                "garmin",
                "hublot",
                "rado",
                "omega",
                "longines"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("fashionwatches")
        } else if (listOf(
                "applemacbookvariant",
                "macbook",
                "applelaptop",
                "macbookpro",
                "macbookair",
                "m1macbook",
                "m2macbook",
                "macos",
                "applem1",
                "applem2",
                "macbookm1",
                "macbookm2",
                "applemac",
                "retinamac",
                "macnotebook",
                "macbookaccessories"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("applemacbookvarients")
        } else if (listOf(
                "fashionshoes",
                "sneaker",
                "nike",
                "adidas",
                "puma",
                "reebok",
                "vans",
                "jordans",
                "converse",
                "asics",
                "underarmour",
                "fila",
                "woodland",
                "newbalance",
                "crocs",
                "louisvuittonshoes"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("fashionshoes")
        } else if (listOf(
                "camera",
                "dslr",
                "mirrorless",
                "canon",
                "nikon",
                "sonyalpha",
                "gopro",
                "fujifilm",
                "olympus",
                "panasoniclumix",
                "leica",
                "instax",
                "blackmagic",
                "cinemacamera",
                "polaroid",
                "securitycamera"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("cameras")
        } else if (listOf(
                "fashionsunglasses",
                "sunglasses",
                "rayban",
                "oakley",
                "fastrack",
                "prada",
                "burberry",
                "gucci",
                "versace",
                "armani",
                "policeglasses",
                "maui",
                "dolcegabbanaglasses",
                "aviators",
                "wayfarers",
                "cateyeglasses",
                "polarizedsunglasses"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("fashionsunglasses")
        } else if (listOf(
                "musicalinstrument",
                "guitar",
                "piano",
                "violin",
                "flute",
                "ukulele",
                "trumpet",
                "harmonica",
                "drums",
                "synthesizer",
                "cajon",
                "tabla",
                "saxophone",
                "clarinet",
                "cello"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("musicalinstruments")
        } else if (listOf(
                "vehicle",
                "car",
                "bike",
                "scooter",
                "electricbike",
                "bicycle",
                "honda",
                "tata",
                "hyundai",
                "bajaj",
                "royalenfield",
                "suzuki",
                "toyota",
                "ford",
                "audi",
                "bmw"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("vehicles")
        } else if (listOf(
                "homeappliance",
                "refrigerator",
                "washingmachine",
                "microwave",
                "airconditioner",
                "geyser",
                "vacuumcleaner",
                "fan",
                "mixergrinder",
                "waterpurifier",
                "heater",
                "airpurifier",
                "chimney",
                "dishwasher",
                "cooler"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("homeappliances")
        } else if (listOf(
                "book",
                "novel",
                "fiction",
                "nonfiction",
                "textbook",
                "comic",
                "mysterybook",
                "biographies",
                "selfhelpbook",
                "mangabook",
                "academicbook",
                "engineeringbook",
                "medicalbook",
                "businessbook",
                "literaturebooks"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("books")
        } else if (listOf(
                "engineeringnote",
                "note",
                "lecturematerial",
                "studyresource",
                "mechanicalnote",
                "electricalnote",
                "civilnote",
                "computersciencenote",
                "itnote",
                "electronicnote",
                "chemicalnote",
                "physicsnote",
                "mathematicsnote",
                "aeronote",
                "biotechnotes"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("engineeringnotes")
        } else if (listOf(
                "tablet",
                "ipad",
                "samsungtablet",
                "androidtablet",
                "lenovotablet",
                "huaweitablet",
                "realmetablet",
                "mixtablet",
                "delltablet",
                "windowsurface",
                "kindle",
                "firetablet",
                "tab",
                "chrometablet",
                "asuszenpad",
                "teclasttablet"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("tablets")
        } else if (listOf(
                "charger",
                "fastcharger",
                "wirelesscharger",
                "typeccharger",
                "iphonecharger",
                "samsungcharger",
                "redmicharger",
                "onepluscharger",
                "oppocharger",
                "vivotypec",
                "realmecharger",
                "laptopcharger",
                "macbookcharger",
                "ankercharger",
                "belkincharger"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("chargers")
        } else if (listOf(
                "powerbank",
                "mi powerbank",
                "redmipowerbank",
                "samsungpowerbank",
                "onepluspowerbank",
                "realmepowerbank",
                "ankerpowerbank",
                "ambrane",
                "energizerpowerbank",
                "duracellpowerbank",
                "asuspowerbank",
                "oppo_powerbank",
                "vivopowerbank",
                "infinixpowerbank",
                "huawei_powerbank",
                "magsafepowerbank"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("powerbanks")
        } else if (listOf(
                "keyboard",
                "mechanicalkeyboard",
                "wirelesskeyboard",
                "bluetoothkeyboard",
                "logitechkeyboard",
                "razerkeyboard",
                "corsairkeyboard",
                "applekeyboard",
                "microsoftkeyboard",
                "dellkeyboard",
                "hpkeyboard",
                "redragonkeyboard",
                "steelserieskeyboard",
                "gamingkeyboard",
                "rgbkeyboard",
                "membranekeyboard"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("keyboards")
        } else if (listOf(
                "calculator",
                "scientificcalculator",
                "casiocalculator",
                "texasinstrument",
                "basiccalculator",
                "graphingcalculator",
                "fx991ex",
                "fx991m",
                "hpcalculator",
                "sharp_calculator",
                "engineeringcalculator",
                "financialcalculator",
                "programmablecalculator",
                "deskcalculator",
                "solarcalculator",
                "mini_calculator"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("calculators")
        } else if (listOf(
                "backpack",
                "schoolbag",
                "laptopbag",
                "travelbackpack",
                "hikingbackpack",
                "trekkingbag",
                "collegebag",
                "rucksack",
                "dufflebag",
                "daypack",
                "samsonitebackpack",
                "wildcraftbackpack",
                "fastrackbackpack",
                "nikebackpack",
                "adidasbackpack"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("backpacks")
        } else if (listOf(
                "furnituredesksandchair",
                "desk",
                "officedesk",
                "sturdytable",
                "woodendesk",
                "computertable",
                "studytable",
                "workstation",
                "ergonomicchair",
                "gamingchair",
                "revolvingchair",
                "executivechair",
                "foldingchair",
                "plasticchair",
                "woodenchair",
                "adjustablechair"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("furnituredesksandchairs")
        } else if (listOf(
                "mattress",
                "memoryfoam",
                "orthopedicmattress",
                "springmattress",
                "latexfoam",
                "singlemattress",
                "doublemattress",
                "queenmattres",
                "kingmattres",
                "cottonmattres",
                "foldablemattres",
                "sleepwell",
                "tempur",
                "duroflex",
                "pepsmattres",
                "kurlon"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("mattresses")
        } else if (listOf(
                "roomdecor",
                "homedecor",
                "wallart",
                "poster",
                "fairylight",
                "stringlight",
                "ledlight",
                "photoframe",
                "wallsticker",
                "tablelamp",
                "candle",
                "clock",
                "showpiece",
                "artificialplant",
                "cushions"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("room_decor")
        } else if (listOf(
                "sportsequipment",
                "badmintonracket",
                "cricketbat",
                "football",
                "basketball",
                "volleyball",
                "tennisracket",
                "hockeystick",
                "tabletennisset",
                "gymdumbbell",
                "kettlebell",
                "yogamat",
                "skippingrope",
                "boxingglove",
                "resistanceband",
                "cyclinghelmet"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("sportsequipment")
        } else if (listOf(
                "bicycle",
                "mountainbike",
                "roadbike",
                "hybridbike",
                "electricbike",
                "foldingbike",
                "bmx",
                "gearcycle",
                "racerbike",
                "citybike",
                "herocycle",
                "atlascycle",
                "firefoxbike",
                "giantbike",
                "meridabike"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("bicycles")
        } else if (listOf(
                "printeraccessorie",
                "printercartridge",
                "toner",
                "inkrefill",
                "lasertoner",
                "printerpaper",
                "photopaper",
                "printercable",
                "inkjetcartridge",
                "hpprinterink",
                "epsonink",
                "canonink",
                "brotherprinterink",
                "thermalpaper",
                "printerhead",
                "printerrollers"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("printeraccessories")
        } else if (listOf(
                "stationery",
                "notebook",
                "pen",
                "diary",
                "journal",
                "gelpen",
                "ballpen",
                "fountainpen",
                "mechanicalpencil",
                "sketchbook",
                "highlighter",
                "eraser",
                "sharpener",
                "stickynote",
                "markers"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("stationary")
        } else if (listOf(
                "softwarelicense",
                "windowslicense",
                "msofficelicense",
                "adobecc",
                "photoshoplicense",
                "autocadlicense",
                "microsoft365",
                "antiviruslicense",
                "vpnsubscription",
                "zoompro",
                "cloudstorage",
                "spotifypremium",
                "netflixsubscription",
                "steamkey",
                "playstoregiftcard",
                "itunesgiftcard"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("softwarelicense")
        } else if (listOf(
                "projector",
                "miniprojector",
                "hdprojector",
                "4kprojector",
                "homecinema",
                "officemultimedia",
                "epsonprojector",
                "benqprojector",
                "viewsonicprojector",
                "sonyprojector",
                "lgprojector",
                "ankerprojector",
                "pico_projector",
                "laserprojector",
                "wirelessprojector"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("projectors")
        } else if (listOf(
                "monitor",
                "gamingmonitor",
                "curvedmonitor",
                "ledmonitor",
                "ipsmonitor",
                "144hzmonitor",
                "ultrawidemonitor",
                "4kmonitor",
                "asusmonitor",
                "lgmonitor",
                "samsungmonitor",
                "dellmonitor",
                "benqmonitor",
                "acerpredator",
                "hpmonitor",
                "viewsonicmonitor"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("monitors")
        } else if (listOf(
                "harddrive",
                "externalhdd",
                "portablehdd",
                "internalhdd",
                "seagatehdd",
                "wdhdd",
                "toshibahdd",
                "samsunghdd",
                "1tbhdd",
                "2tbhdd",
                "laptophdd",
                "desktophdd",
                "surveillancehdd",
                "gaminghdd",
                "raidstorage"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("harddrives")
        } else if (listOf(
                "flashdrive",
                "usbflashdrive",
                "pen_drive",
                "otgpendrive",
                "typecpdrive",
                "sandiskpendrive",
                "kingstonpendrive",
                "hpflashdrive",
                "toshibaflashdrive",
                "sonyflashdrive",
                "samsungpendrive",
                "32gbpendrive",
                "64gbpendrive",
                "128gbpendrive",
                "encryptedpendrive"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("flashdrives")
        } else if (listOf(
                "gamingconsole",
                "ps5",
                "ps4",
                "xboxseriesx",
                "xboxseriesS",
                "nintendoswitch",
                "steamdeck",
                "gaminghandheld",
                "nintendo",
                "playstation",
                "xboxone",
                "retrogamingconsole",
                "gamingpc",
                "vrgaming",
                "arcadeconsole",
                "nvidiashield",
                "segamegadrive"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("gamingconsoles")
        } else if (listOf(
                "boardgame",
                "ches",
                "monopoly",
                "scrabble",
                "carrom",
                "riskgame",
                "ludo",
                "clueboardgame",
                "catan",
                "dungeonsanddragon",
                "uno",
                "jenga",
                "codename",
                "connect4",
                "battleship",
                "tickettoride"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("board_games")
        } else if (listOf(
                "artsupply",
                "paintbrush",
                "acrylicpaints",
                "watercolor",
                "oilpaint",
                "canvasboard",
                "sketchpencil",
                "charcoalpencil",
                "coloredpencil",
                "pastelcolor",
                "marker",
                "calligraphyset",
                "easel",
                "paletteknife",
                "drawingpaper",
                "sculptingtools",
                "art",
                "brushes",
                "brush",
                "acrylic"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("artsupplies")
        } else if (listOf(
                "sewingmachine",
                "stitchingmachine",
                "tailoringmachine",
                "handheldsewingmachine",
                "electricsewingmachine",
                "mechanicalsewingmachine",
                "embroiderymachine",
                "brothersewingmachine",
                "singersewingmachine",
                "janomesewingmachine",
                "berninasm",
                "sewingkit",
                "overlockmachine",
                "quiltingmachine",
                "zigzagsewingmachine"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("sewingmachines")
        } else if (listOf(
                "kitchenappliance",
                "blender",
                "mixergrinder",
                "foodprocessor",
                "microwaveoven",
                "toaster",
                "inductionstove",
                "airfryer",
                "pressurecooker",
                "electrickettle",
                "coffeemaker",
                "ricecooker",
                "juicer",
                "dishwasher",
                "handmixer",
                "sandwichmaker"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("kitchenappliances")
        } else if (listOf(
                "lamp",
                "tablelamp",
                "studytablelamp",
                "ledlamp",
                "readinglamp",
                "nightlamp",
                "floorlamp",
                "desklamp",
                "bedsidelamp",
                "cliponlamp",
                "touchlamp",
                "adjustablelamp",
                "decorativelamp",
                "fairylightslamp",
                "solarledlamp"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("lamps")
        } else if (listOf(
                "clock",
                "wallclock",
                "alarmclock",
                "digitalclock",
                "tableclock",
                "pendulumclock",
                "smartclock",
                "analogueclock",
                "decorativeclock",
                "ledclock",
                "projectionclock",
                "silentclock",
                "vintageclock",
                "atomicclock",
                "travelclock"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("clocks")
        } else if (listOf(
                "waterbottle",
                "thermosbottle",
                "stainlesssteelbottle",
                "plasticwaterbottle",
                "glasswaterbottle",
                "copperbottle",
                "hydrationbottle",
                "sportsbottle",
                "collapsiblebottle",
                "filteredwaterbottle",
                "sipperbottle",
                "infuserbottle",
                "bpakwaterbottle",
                "miltonbottle",
                "camelbakbottle",
                "nalgene"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("waterbottles")
        } else if (listOf(
                "labcoat",
                "whitecoat",
                "medicalcoat",
                "scientistcoat",
                "doctorcoat",
                "chemistcoat",
                "longlabcoat",
                "shortlabcoat",
                "cottonlabcoat",
                "polyesterlabcoat",
                "disposablelabcoat",
                "protectivelabcoat",
                "buttonedlabcoat",
                "zipperedlabcoat",
                "kidslabcoat"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("labcoats")
        } else if (listOf(
                "safetygoggle",
                "labgoggle",
                "chemistrygoggle",
                "protectivegoggle",
                "sciencegoggle",
                "uvprotectiongoggle",
                "splashresistantgoggle",
                "antifoggoggle",
                "medicalgoggle",
                "lasergoggle",
                "dustproofgoggle",
                "adjustablegoggle",
                "clearvisiongoggle",
                "industrialgoggle",
                "chemicalgoggles"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("safetygoggles")
        } else if (listOf(
                "graphingpaper",
                "mathgraphpaper",
                "engineeringgraphpaper",
                "millimeterpaper",
                "logarithmicgraphpaper",
                "isometricgraphpaper",
                "polargraphpaper",
                "gridpaper",
                "hexagonalgraphpaper",
                "blueprintgraphpaper",
                "coordinategraphpaper",
                "labgraphpaper",
                "handwritinggraphpaper",
                "quadruledpaper",
                "plaingraphpaper"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("graphingpaper")
        } else if (listOf(
                "labequipment",
                "microscope",
                "compoundmicroscope",
                "stereomicroscope",
                "digitalmicroscope",
                "fluorescencemicroscope",
                "biologicalmicroscope",
                "electronmicroscope",
                "telescope",
                "centrifuge",
                "spectrometer",
                "testtube",
                "pipette",
                "beaker",
                "flask",
                "bunsenburner"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("labequipment&microscopes")
        } else if (listOf(
                "literaturebook",
                "classicbook",
                "shakespearebook",
                "poetrybook",
                "englishliterature",
                "dickensbook",
                "janeaustenbook",
                "hemingwaybook",
                "tolkienbook",
                "orwellbook",
                "dostoevskybook",
                "victorianliterature",
                "modernistbook",
                "philosophybook",
                "historicalfiction"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("literaturebooks")
        } else if (listOf(
                "fictionnovel",
                "bestsellingfiction",
                "mysterynovel",
                "thrillernovel",
                "romanticnovel",
                "fantasynovel",
                "scifinovel",
                "youngadultnovel",
                "historicalfictionnovel",
                "horrornovel",
                "dystopianfiction",
                "graphicnovel",
                "literaryfiction",
                "adventurenovel",
                "crimefiction"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("fictionnovels")
        } else if (listOf(
                "journal",
                "diarynotebook",
                "bulletjournal",
                "traveljournal",
                "studyjournal",
                "gratitudejournal",
                "linedjournal",
                "dotgridjournal",
                "plannerjournal",
                "sketchjournal",
                "hardcoverjournal",
                "softcoverjournal",
                "recycledpaperjournal",
                "goalsettingjournal",
                "spiraljournal"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("journals")
        } else if (listOf(
                "ereader",
                "kindle",
                "kindlepaperwhite",
                "kindleoasi",
                "kindlescribe",
                "nook",
                "koboreader",
                "sonyereader",
                "pocketbookereader",
                "inkpadereader",
                "touchscreenereader",
                "pdfereader",
                "comicereader",
                "waterproofereader",
                "einkreader",
                "tabletforreading"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("ereaders")
        } else if (listOf(
                "poster",
                "dormposter",
                "studyroomposter",
                "motivationalposter",
                "animeposter",
                "gamingposter",
                "sportsstarposter",
                "celebrityposter",
                "wallartposter",
                "movieposter",
                "aestheticposter",
                "minimalistposter",
                "musicbandposter",
                "comicposter",
                "abstractartposters"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("posters(fordormhostelsrooms,studyspaces)")
        } else if (listOf(
                "planner",
                "studynotebook",
                "assignmentplanner",
                "projectplanner",
                "dailyplanner",
                "weeklyplanner",
                "monthlyplanner",
                "academicplanner",
                "goaltracker",
                "todolist",
                "journalplanner",
                "productivityplanner",
                "habittracker",
                "bulletjournal",
                "digitalplanner",
                "timeblockplanner"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("planners(forstudy,assignments,projects)")
        } else if (listOf(
                "coffeemaker",
                "espresso",
                "frenchpres",
                "dripcoffeemaker",
                "nespresso",
                "keurig",
                "aeropres",
                "coldbrewmaker",
                "moka",
                "pour-overcoffeemaker",
                "siphoncoffeemaker",
                "capsulecoffeemachine",
                "single-servecoffeemaker",
                "thermalcoffeemaker",
                "manualcoffeemaker"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("coffeemakers(forlate-nightstudying&hustlemode)")
        } else if (listOf(
                "curtain",
                "windowcurtain",
                "blackoutcurtain",
                "sheercurtain",
                "thermalcurtain",
                "soundproofcurtain",
                "printedcurtain",
                "velvetcurtain",
                "cottoncurtain",
                "polyestercurtain",
                "lincurtain",
                "grommetcurtain",
                "rodpocketcurtain",
                "tabtopcurtain",
                "pleatedcurtains"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("curtains")
        } else if (listOf(
                "bedsheet",
                "cottonbedsheet",
                "kingbedsheet",
                "queenbedsheet",
                "singlebedsheet",
                "microfiberbedsheet",
                "silkbedsheet",
                "flannelbedsheet",
                "hotelstylebedsheet",
                "printedbedsheet",
                "fittedbedsheet",
                "bamboo-fabricbedsheet",
                "satinbedsheet",
                "organiccottonbedsheet",
                "coolingbedsheets"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("bedsheets")
        } else if (listOf(
                "pillow",
                "memoryfoampillow",
                "orthopedicpillow",
                "featherpillow",
                "downpillow",
                "bodypillow",
                "coolingpillow",
                "cottonpillow",
                "necksupportpillow",
                "travelpillow",
                "lumbarpillow",
                "pregnancypillow",
                "hypoallergenicpillow",
                "latexfoam-pillow",
                "gelinfusedpillow",
                "sleepingpillow"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("pillows")
        } else if (listOf(
                "rug",
                "arearug",
                "bedroomrug",
                "carpetrug",
                "woolrug",
                "cottonrug",
                "juterug",
                "shagrug",
                "turkishrug",
                "modernrug",
                "traditionalrug",
                "kilimrug",
                "handwovenrug",
                "silkrug",
                "outdoorrug",
                "printedrug"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("rugs")
        } else if (listOf(
                "fan",
                "tablefan",
                "ceilingfan",
                "pedestalfan",
                "exhaustfan",
                "towerfan",
                "bladelessfan",
                "personaldeskfan",
                "oscillatingfan",
                "industrialfan",
                "minifan",
                "wallmountfan",
                "handheldfan",
                "usbfan",
                "coolingfan",
                "dcceilingfan"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("fans")
        } else if (listOf(
                "umbrella",
                "foldingumbrella",
                "compactumbrella",
                "golfumbrella",
                "automaticumbrella",
                "windproofumbrella",
                "transparentumbrella",
                "stickumbrella",
                "travelumbrella",
                "largeumbrella",
                "bubbleumbrella",
                "kidsumbrella",
                "reversefoldingumbrella",
                "uvprotectionumbrella",
                "fashionumbrella",
                "rainumbrella"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("umbrellas")
        } else if (listOf(
                "raincoat",
                "ponchoraincoat",
                "longraincoat",
                "waterproofjacket",
                "trenchraincoat",
                "hoodedraincoat",
                "plasticraincoat",
                "disposableraincoat",
                "packableraincoat",
                "lightweightcoat",
                "reflectiveraincoat",
                "fashionraincoat",
                "transparentraincoat",
                "kidsraincoat",
                "cyclingraincoat",
                "hikingraincoat"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("raincoats")
        } else if (listOf(
                "shoe",
                "shoes",
                "sneaker",
                "formal",
                "casualshoe",
                "loafer",
                "boot",
                "derbyshoe",
                "oxfordshoe",
                "running-shoe",
                "training-shoe",
                "basketballshoe",
                "trekkingshoe",
                "high-topshoe",
                "sliponshoe",
                "canvas-shoe",
                "leathershoes"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("fashionshoes")
        } else if (listOf(
                "sportsjersey",
                "footballjersey",
                "basketballjersey",
                "cricketjersey",
                "soccerjersey",
                "baseballjersey",
                "rugbeyjersey",
                "cyclingjersey",
                "athleticsjersey",
                "customteamjersey",
                "fansupportjersey",
                "gymwearjersey",
                "hockeyjersey",
                "badmintonjersey",
                "tennisjersey"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("sportsjerseys")
        } else if (listOf(
                "dumbbells",
                "hexagonaldumbbell",
                "adjustabledumbbell",
                "fixedweightdumbbell",
                "rubbercoateddumbbell",
                "neoprenedumbbell",
                "metallicdumbbell",
                "lightweightdumbbell",
                "gymdumbbell",
                "aerobicdumbbell",
                "heavyduty-dumbbell",
                "castirondumbbell",
                "vinylcoateddumbbell",
                "beginnersdumbbell",
                "strengthtrainingdumbbell",
                "proweightdumbbells"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("dumbbells")
        } else if (listOf(
                "yogamat",
                "non-slipyogamat",
                "eco-friendlyyogamat",
                "tpeyogamat",
                "pvcfreeyogamat",
                "cottonyogamat",
                "microfiberyogamat",
                "lightweightyogamat",
                "thickyogamat",
                "travel-friendlyyogamat",
                "bamboo-yogamat",
                "proyogamat",
                "corkyogamat",
                "foldableyogamat",
                "doublelayeryogamat",
                "suedeyogamat"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("yogamats")
        } else if (listOf(
                "helmet",
                "motorcyclehelmet",
                "bicyclehelmet",
                "scooterhelmet",
                "fullfacehelmet",
                "half-facehelmet",
                "modularhelmet",
                "offroadhelmet",
                "openfacehelmet",
                "carbonfiberhelmet",
                "dotcertifiedhelmet",
                "dual-sporthelmet",
                "safetyhelmet",
                "racinghelmet",
                "smarthelmet",
                "customgraphichelmet"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("helmets")
        } else if (listOf(
                "skateboard",
                "longboard",
                "cruiserboard",
                "pennyskateboard",
                "electricskateboard",
                "miniskateboard",
                "freestyleskateboard",
                "doublekickskateboard",
                "dropthroughskateboard",
                "downhillskateboard",
                "streetboard",
                "offroadskateboard",
                "bambooskateboard",
                "fiberglassskateboard",
                "customskateboard",
                "proskateboard"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("skateboards")
        } else if (listOf(
                "makeupkit",
                "beautyset",
                "cosmeticset",
                "foundationkit",
                "lipstickset",
                "eyeshadowpalette",
                "contourkit",
                "highlighterkit",
                "blushpalette",
                "makeupbrushe",
                "fullfacekit",
                "professionalmakeupkit",
                "bridalmakeupkit",
                "compactmakeupset",
                "travelmakeupkit",
                "waterproofmakeupkit"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("makeupkits")
        } else if (listOf(
                "hairdryer",
                "blowdryer",
                "ionicdryer",
                "salonhairdryer",
                "travelhairdryer",
                "compacthairdryer",
                "foldablehairdryer",
                "lightweighthairdryer",
                "ceramichairdryer",
                "turbohairdryer",
                "professioinalhairdryer",
                "cordedhairdryer",
                "cordlesshairdryer",
                "infraredhairdryer",
                "highspeedhairdryer",
                "low-noisehairdryer"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("hairdryers")
        } else if (listOf(
                "hairstraightener",
                "hairiron",
                "flatiron",
                "ceramichairstraightener",
                "titaniumhairstraightener",
                "wet-to-drystraightener",
                "ionicstraightener",
                "portablehairiron",
                "minihairstraightener",
                "cordlesshairstraightener",
                "infraredstraightener",
                "digitalhairstraightener",
                "salonhairstraightener",
                "professioinalhairstraightener",
                "widerplatehairiron",
                "steamhairstraightener"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("hairstraighteners")
        } else if (listOf(
                "wallet",
                "leatherwallet",
                "bifoldwallet",
                "trifoldwallet",
                "slimwallet",
                "moneyclipwallet",
                "rfidwallet",
                "zipperwallet",
                "cardholderwallet",
                "minimalistwallet",
                "travelwallet",
                "chainwallet",
                "fabricwallet",
                "coinpouchwallet",
                "sportswallet",
                "handmadewallet"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("wallets")
        } else if (listOf(
                "watch",
                "analogwatch",
                "digitalwatch",
                "smartwatch",
                "sportswatch",
                "diverwatch",
                "automaticwatch",
                "luxurywatch",
                "chronographwatch",
                "leatherstrapwatch",
                "metalstrapwatch",
                "rubberstrapwatch",
                "hybridwatch",
                "vintagewatch",
                "minimalistwatch",
                "titaniumwatch"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("watches")
        } else if (listOf(
                "sunglass",
                "sunglasses",
                "aviatorsunglasse",
                "wayfarersunglasse",
                "polarizedsunglasse",
                "sportsunglasse",
                "mirrorsunglasse",
                "roundsunglasse",
                "cat-eyesunglasse",
                "oversizesunglasse",
                "retrosunglasse",
                "clip-onsunglasse",
                "photochromicsunglasse",
                "fashionablesunglasse",
                "outdoorsunglasse",
                "classicbrowlinesunglasse",
                "designerbrandedsunglasses"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("sunglasses")
        } else if (listOf(
                "jewelry",
                "goldjewelry",
                "silverjewelry",
                "diamondjewelry",
                "fashionjewelry",
                "bracelet",
                "necklace",
                "ring",
                "earring",
                "anklet",
                "pendant",
                "choker",
                "gemstonejewelry",
                "customjewelry",
                "luxuryjewelry",
                "handmadejewelry"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("jewelry")
        } else if (listOf(
                "flashlight",
                "torchlight",
                "ledflashlight",
                "rechargeableflashlight",
                "solarflashlight",
                "tacticalflashlight",
                "miniflashlight",
                "waterproofflashlight",
                "headlampflashlight",
                "usbflashlight",
                "handheldflashlight",
                "emergencyflashlight",
                "zoomableflashlight",
                "campingflashlight",
                "highpowerflashlight"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("flashlights")
        } else if (listOf(
                "toolkit",
                "handtoolkit",
                "mechanicstoolkit",
                "householdtoolkit",
                "precisiontoolkit",
                "powerdrillset",
                "screwdrivertoolkit",
                "plumbingtoolkit",
                "carrepairtoolkit",
                "multipurposetoolkit",
                "electricaltoolkit",
                "woodworkingtoolkit",
                "diytoolkit",
                "engineertoolkit",
                "compacttoolkit",
                "professionaltoolkit"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("toolkits")
        } else if (listOf(
                "screwdriver",
                "flatheadscrewdriver",
                "phillipsscrewdriver",
                "torxscrewdriver",
                "hexscrewdriver",
                "multibitsscrewdriver",
                "electricscrewdriver",
                "magneticscrewdriver",
                "precisionscrewdriver",
                "insulatedscrewdriver",
                "longhandlescrewdriver",
                "stubbyhandlescrewdriver",
                "ratchetingscrewdriver",
                "jewelrystoolkit",
                "flexibleshaftscrewdriver",
                "pocketclipsscrewdriver"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("screwdrivers")
        } else if (listOf(
                "extensioncord",
                "powerstrip",
                "longextensioncord",
                "surgeprotectedextensioncord",
                "outdoorextensioncord",
                "usbextensioncord",
                "multi-plugextensioncord",
                "heavy-dutyextensioncord",
                "angledplugextensioncord",
                "shortextensioncord",
                "retractableextensioncord",
                "cablemanagementextensioncord",
                "thinflatcableextensioncord",
                "coilextensioncord",
                "wall-mountedextensioncord"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("extensioncords")
        } else if (listOf(
                "studylamp",
                "desklamp",
                "ledstudylamp",
                "adjustablestudylamp",
                "clip-onstudylamp",
                "dimmablestudylamp",
                "architectstudylamp",
                "touchcontrolstudylamp",
                "portablelamp",
                "warmwhitestudylamp",
                "coolwhitestudylamp",
                "batteryoperatedstudylamp",
                "usbpoweredstudylamp",
                "foldablestudylamp",
                "smartstudylamp"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("lamps")
        } else if (listOf(
                "ethernetcable",
                "cat5ethernetcable",
                "cat6ethernetcable",
                "cat7ethernetcable",
                "cat8ethernetcable",
                "shieldedethernetcable",
                "flatethernetcable",
                "highspeedethernetcable",
                "goldplatedethernetcable",
                "rj45ethernetcable",
                "gamingethernetcable",
                "longethernetcable",
                "shortethernetcable",
                "waterproofethernetcable",
                "braidedsleeveethernetcable",
                "slimethernetcable"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("ethernetcables")
        } else if (listOf(
                "usb",
                "usb2.0",
                "usb3.0",
                "usbcable",
                "usbhub",
                "usbadapter",
                "usbflashdrive",
                "usbtypec",
                "lightningusb",
                "micro-usb",
                "miniusb",
                "highspeedusb",
                "usbextension",
                "usbcharger",
                "usbdatacable"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("usb")
        } else if (listOf(
                "hdmicable",
                "hdmi",
                "hdmi2.0",
                "hdmi2.1",
                "4khdmi",
                "8khdmi",
                "highspeedhdmi",
                "goldplatedhdmi",
                "slimhdmi",
                "mini-hdmi",
                "micro-hdmi",
                "braidedhdmi",
                "hdmitousbc",
                "hdmitovga",
                "hdmitodvi",
                "hdmisplitter"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("hdmi")
        } else if (listOf(
                "headphonesplitter",
                "audiosplitter",
                "stereosplitter",
                "3.5mmsplitter",
                "dualheadphonesplitter",
                "micandaudiosplitter",
                "y-splitter",
                "lightningsplitter",
                "usbcsplitter",
                "bluetoothsplitter",
                "wirelessheadphonesplitter",
                "auxsplitter",
                "splitteradapter",
                "gamingheadsetsplitter",
                "hdsplitter"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("headphone_splitters")
        } else if (listOf(
                "headphones",
                "earphones",
                "headphone",
                "earphone",
                "boat",
                "wirelessheadphones",
                "bluetoothheadphones",
                "overearheadphones",
                "onearheadphones",
                "noisecancelingheadphones",
                "gamingheadphones",
                "studioheadphones",
                "bassboostheadphones",
                "sportheadphones",
                "foldableheadphones",
                "wiredheadphones",
                "hifiheadphones",
                "djheadphones",
                "travelheadphones",
                "usbheadphones"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("headphones")
        } else if (listOf(
                "bookshelf",
                "woodenbookshelf",
                "metalbookshelf",
                "floatingbookshelf",
                "cornerbookshelf",
                "ladderbookshelf",
                "glassbookshelf",
                "modularbookshelf",
                "walldecorbookshelf",
                "compactbookshelf",
                "tallbookshelf",
                "minimalistbookshelf",
                "smallspacebookshelf",
                "adjustablebookshelf",
                "multilayerbookshelf",
                "diybookshelf"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("bookshelves")
        } else if (listOf(
                "whiteboard",
                "dryeraseboard",
                "magneticwhiteboard",
                "glasswhiteboard",
                "portablewhiteboard",
                "smallwhiteboard",
                "classroomwhiteboard",
                "officewhiteboard",
                "framelesswhiteboard",
                "flipchartwhiteboard",
                "rollingwhiteboard",
                "double-sidedwhiteboard",
                "stickablewhiteboard",
                "deskwhiteboard",
                "digitalwhiteboard",
                "kidswhiteboard"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("whiteboards")
        } else if (listOf(
                "marker",
                "permanentmarker",
                "whitemarker",
                "blackmarker",
                "coloredmarker",
                "dryerasemarker",
                "highlightermarker",
                "boardmarker",
                "finepointmarker",
                "calligraphymarker",
                "paintmarker",
                "artisticmarker",
                "dual-tipmarker",
                "fluorescentmarker",
                "waterproofmarker"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("markers")
        } else if (listOf(
                "clipboard",
                "woodenclipboard",
                "plasticclipboard",
                "metalclipboard",
                "foldingclipboard",
                "a4clipboard",
                "clipboardwithstorage",
                "personalizedclipboard",
                "miniclipboard",
                "documentholderclipboard",
                "clipboardsheet",
                "officeclipboard",
                "presentationclipboard",
                "sturdyclipboard",
                "notepadclipboard",
                "portableclipboard"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("clipboards")
        } else if (listOf(
                "tshirt",
                "cottonshirt",
                "graphictee",
                "roundnecktshirt",
                "v-necktshirt",
                "oversizetshirt",
                "slimfittshirt",
                "plainwhitetshirt",
                "pockettshirt",
                "polo-shirt",
                "sportsdryfittshirt",
                "longsleevetshirt",
                "crop-top",
                "customtshirt",
                "brandedtshirt",
                "trendyprintedtshirt"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("tshirts")
        } else if (listOf(
                "hoodie",
                "zipperhoodie",
                "pulloverhoodie",
                "fleecehoodie",
                "sleevelesshoodie",
                "croppedhoodie",
                "graphicprintshoodie",
                "sportswearhoodie",
                "winterhoodie",
                "streetwearhoodie",
                "plaincolorhoodie",
                "oversizedhoodie",
                "thermohoodie",
                "fashionablehoodie",
                "comfyhoodie",
                "casualhoodie"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("hoodies")
        } else if (listOf(
                "jean",
                "denimjean",
                "skinnyjean",
                "slimfitjean",
                "regularfitjean",
                "bootcutjean",
                "highwaistjean",
                "lowrisejean",
                "rippedjean",
                "stretchablejean",
                "blackdenimjean",
                "bluejean",
                "boyfriendjean",
                "baggyjean",
                "cargojean",
                "straightcutjeans"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("jeans")
        } else if (listOf(
                "dress",
                "cocktaildress",
                "casualdress",
                "formaldress",
                "sundress",
                "bodycondress",
                "wrapdress",
                "maxidress",
                "minidress",
                "midi dress",
                "partywear",
                "floralprintdress",
                "offshoulderdress",
                "cottondress",
                "satinfabricdress",
                "weddingdress"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("dresses")
        } else if (listOf(
                "coat",
                "wintercoat",
                "trenchcoat",
                "pea-coat",
                "overcoat",
                "leathercoat",
                "furcoat",
                "woolencoat",
                "puffercoat",
                "denimcoat",
                "militarycoat",
                "longcoat",
                "shortcoat",
                "stylishcoat",
                "formalfittedcoat",
                "casualcoat"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("coats")
        } else if (listOf(
                "glove",
                "winterglove",
                "leatherglove",
                "woolglove",
                "fingerlessglove",
                "touchscreenglove",
                "sportsglove",
                "bikingglove",
                "drivingglove",
                "gardeningglove",
                "heatresistantglove",
                "cottonknitglove",
                "rubberglove",
                "gymglove",
                "workglove",
                "insulatedgloves"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("gloves")
        } else if (listOf(
                "cap",
                "baseballcap",
                "truckerhat",
                "snapbackcap",
                "dadcap",
                "beaniecap",
                "bucketcap",
                "flatcap",
                "visorcap",
                "sportsrunningcap",
                "sunprotectioncap",
                "adjustablecap",
                "woolcap",
                "canvascap",
                "stylishcap",
                "designerbrandcap"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("caps")
        } else if (listOf(
                "blanket",
                "fleeceblanket",
                "cottonblanket",
                "woolblanket",
                "heatedblanket",
                "weightedblanket",
                "throwblanket",
                "quiltedblanket",
                "duvetblanket",
                "lightweightblanket",
                "campingblanket",
                "travelblanket",
                "winterblanket",
                "summerblanket",
                "bedroomblanket",
                "knittedblanket"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("blankets")
        } else if (listOf(
                "crockery",
                "ceramiccrockery",
                "porcelaincrockery",
                "glasscrockery",
                "stainlesssteelcrockery",
                "dinnerplate",
                "bowlandspoon",
                "teacup",
                "mugset",
                "servingtray",
                "cutleryset",
                "stonewarecrockery",
                "finediningcrockery",
                "customcrockery",
                "kidsfriendlycrockery",
                "microwavesafecrockery"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("crockery")
        } else if (listOf(
                "waterfilter",
                "rofilter",
                "ufwaterfilter",
                "gravitywaterfilter",
                "portablewaterfilter",
                "uvpurifier",
                "homewaterfilter",
                "activatedcarbonfilter",
                "alkalinewaterfilter",
                "countertopfilter",
                "under-sinkfilter",
                "refrigeratorwaterfilter",
                "faucetwaterfilter",
                "wholehousewaterfilter",
                "reverseosmosiswaterfilter"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("waterfilters")
        } else if (listOf(
                "storageboxe",
                "plasticstoragebox",
                "woodenstoragebox",
                "metalstoragebox",
                "fabricstoragebox",
                "underbedstorage",
                "foldablestoragebox",
                "stackablestoragebox",
                "drawerorganizerbox",
                "airtightstoragebox",
                "toystoragebox",
                "decorativestoragebox",
                "portablelockbox",
                "cardboardstoragebox",
                "kitchenstoragebox",
                "multipurposeorganizer"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("storageboxes")
        } else if (listOf(
                "tupperware",
                "plasticcontainer",
                "airtightcontainer",
                "foodstoragebox",
                "kitchenstorage",
                "microwavesafecontainer",
                "stackablecontainer",
                "bpa-freecontainer",
                "leakproofcontainer",
                "tiffinbox",
                "lunchbox",
                "modulartupperware",
                "freezer-safecontainer",
                "snaplockcontainer",
                "siliconefoodstorage"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("tupperware")
        } else if (listOf(
                "fan",
                "ceilingfan",
                "tablefan",
                "pedestalfan",
                "towerfan",
                "bladelessfan",
                "exhaustfan",
                "wallfan",
                "boxfan",
                "usbportablefan",
                "batteryfan",
                "industrialfan",
                "coolingfan",
                "cliponfan",
                "solar-poweredfan",
                "silentfan"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("fans")
        } else if (listOf(
                "powerstrip",
                "extensionboard",
                "multiplepoweroutlet",
                "surgeprotector",
                "usbpowerstrip",
                "smartpowerstrip",
                "flatplugpowerstrip",
                "travelpowerstrip",
                "desktopchargingstation",
                "longcordpowerstrip",
                "powerbar",
                "6-outletpowerstrip",
                "8-outletpowerstrip",
                "wallmountpowerstrip",
                "industrialpowerstrip"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("powerstrips")
        } else if (listOf(
                "smartspeaker",
                "voiceassistant",
                "echospeaker",
                "googlehome",
                "alexaspeaker",
                "nestaudio",
                "sonosspeaker",
                "bluetoothsmartspeaker",
                "bosehome",
                "jblsmartspeaker",
                "hifismartspeaker",
                "stereosmartspeaker",
                "applehomepod",
                "smartdisplay",
                "multifunctionalspeaker"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("smartspeakers")
        } else if (listOf(
                "vrheadset",
                "virtualreality",
                "oculusrift",
                "quest2",
                "htcvive",
                "playstationvr",
                "steamvr",
                "mixedrealityheadset",
                "standalonevr",
                "wirelessvr",
                "pimaxvr",
                "hpreverb",
                "varjovr",
                "metavr",
                "vrgamingheadset",
                "augmentedrealityheadset"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("vrheadsets")
        } else if (listOf(
                "actioncamera",
                "gopro",
                "insta360",
                "sonyrx0",
                "djiosmoaction",
                "polaroidcube",
                "xiaomiyi",
                "garminvirb",
                "rollei",
                "sjcam",
                "akaso",
                "waterproofactioncam",
                "4kactioncamera",
                "helmetcamera",
                "sportsactioncamera",
                "compactactioncamera"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("actioncameras")
        } else if (listOf(
                "binder",
                "ringbinder",
                "leatherbinder",
                "filefolder",
                "d-ringbinder",
                "spiralbinder",
                "pocketbinder",
                "3-ringbinder",
                "a4binder",
                "notebookbinder",
                "documentorganizer",
                "presentationbinder",
                "portfoliofolder",
                "studentbinder",
                "coloredbinder",
                "personalizedbinder"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("binders")
        } else if (listOf(
                "highlighter",
                "fluorescentmarker",
                "gelhighlighter",
                "pastelhighlighter",
                "erasablehighlighter",
                "dual-tipmarker",
                "liquidhighlighter",
                "chisel-tipmarker",
                "smudge-proofhighlighter",
                "schoolhighlighter",
                "officehighlighter",
                "neonhighlighter",
                "softcolorhighlighter",
                "textmarker",
                "multicolormarker",
                "finepointhighlighter"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("highlighters")
        } else if (listOf(
                "scanner",
                "documentscanner",
                "portablescanner",
                "wirelessscanner",
                "flatbedscanner",
                "photoandfilmscanner",
                "handheldscanner",
                "barcodescanner",
                "multipagescanner",
                "fastscanningdevice",
                "ocrscanner",
                "usbscanner",
                "studentscanner",
                "idscanner",
                "compactscanner",
                "officescanner"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("scanners")
        } else if (listOf(
                "tripod",
                "cameratripod",
                "dslrtripod",
                "flexibletripod",
                "phoneholdertripod",
                "lightweighttripod",
                "miniportabletripod",
                "gimbaltripod",
                "vloggingtripod",
                "tabletoptripod",
                "traveltripod",
                "heavy-dutytripod",
                "adjustabletripod",
                "professionaltripod",
                "tripodwithselfiestick",
                "compacttripod"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("tripods")
        } else if (listOf(
                "mousepad",
                "gamingmousepad",
                "largeextendedmousepad",
                "rgbmousepad",
                "ergonomicmousepad",
                "softfabricmousepad",
                "leathermousepad",
                "hardplasticsurface",
                "wristrestmousepad",
                "waterproofmousepad",
                "wirelesschargingmousepad",
                "customdesignmousepad",
                "studentmousepad",
                "minimalistmousepad",
                "precisionmousepad",
                "deskpad"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("mousepads")
        } else if (listOf(
                "fitnessband",
                "fitnesstracker",
                "smartband",
                "mi-band",
                "fitbit",
                "garminfitnessband",
                "samsungeband",
                "huaweifitband",
                "waterprooffitnessband",
                "bluetoothfitnessband",
                "heartmonitorband",
                "steptracker",
                "activitymonitorband",
                "sleeptrackerband",
                "calorieburnband",
                "sportsfitnessband"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("fitnessbands")
        } else if (listOf(
                "studytable",
                "sturdytable",
                "woodentable",
                "foldingtable",
                "compactstudytable",
                "adjustablestudytable",
                "desk",
                "writingtable",
                "laptoptable",
                "studenttable",
                "workspace",
                "readingtable",
                "hostelstudytable",
                "studyfurniture",
                "studysetup",
                "ergonomicstudytable"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("studytables")
        } else if (listOf(
                "laptop",
                "gaminglaptop",
                "ultrabook",
                "businesslaptop",
                "studentlaptop",
                "macbook",
                "windowslaptop",
                "chromebook",
                "budgetlaptop",
                "desktop",
                "pc",
                "allinonepc",
                "towerpc",
                "minipc",
                "custompc"
            ).any { text.contains(it) }
        ) {
            loadLottieAnimation("laptops")
        }

    }

    var currentLottieFile: String? = null      // class-scope

    // in loadLottieAnimation, needed to figure out how is the thread set, and it's flow. Needed to learn.
    private fun loadLottieAnimation(category: String) {

        if (currentLottieFile != "$category.json") {       // runs only if lottie file is different than previous one, if it is the same, not need to run the same file again
//            Toast.makeText(requireContext(), "Current Lottie File: $currentLottieFile && category.json: $category.json", Toast.LENGTH_SHORT).show()
            val context = requireContext()
            val directory = File(context.filesDir, "ProductCategory")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val localFile = File(directory, "$category.json")
            if (localFile.exists()) {
                val jsonString = localFile.readText() // Read file in IO thread

                binding!!.lottieView.visibility = View.VISIBLE
                currentLottieFile = "$category.json"
                binding!!.lottieView.setAnimationFromJson(jsonString)
                binding!!.lottieView.playAnimation()
                Toast.makeText(requireContext(), "WE ARE IN IF", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "WE ARE IN ELSE", Toast.LENGTH_SHORT).show()
                val lottieAnimationRef = firebaseStorageRef.child("ProductCategory/$category.json")

                // Run in Background Thread using Coroutines
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        lottieAnimationRef.getFile(localFile)
                            .await()  // Use await() to suspend until download completes
                        Log.d("LottieDownload", "File downloaded successfully: ${localFile.path}")

                        val jsonString = localFile.readText() // Read file in IO thread

                        // Switch to Main thread to update UI
                        withContext(Dispatchers.Main) {
                            binding!!.lottieView.visibility = View.VISIBLE
                            currentLottieFile = "$category.json"
                            binding!!.lottieView.setAnimationFromJson(jsonString)
                            binding!!.lottieView.playAnimation()
                        }
                    } catch (e: Exception) {
                        Log.e("LottieDownload", "Failed to download file: ${e.message}")
                        withContext(Dispatchers.Main) {
//                        Toast.makeText(requireContext(), "$e", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun loadLottieAnimation2(category: String) {
        val lottieAnimationRef = firebaseStorageRef.child("ProductCategory/$category")

//        if(currentLottieFile!="$category.json" || viewModel.lottieInNameFragment.value != null) {       // runs only if lottie file is differnt than previous one, if it is the same, not need to run the same file again

//            Toast.makeText(requireContext(), "PLAYED: $category", Toast.LENGTH_SHORT).show()
        val context = requireContext()
        val directory = File(context.filesDir, "ProductCategory")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val localFile = File(directory, "$category")

        // Run in Background Thread using Coroutines
        CoroutineScope(Dispatchers.IO).launch {
            try {
                lottieAnimationRef.getFile(localFile)
                    .await()  // Use await() to suspend until download completes
                Log.d("LottieDownload", "File downloaded successfully: ${localFile.path}")

                val jsonString = localFile.readText() // Read file in IO thread

                // Switch to Main thread to update UI
                withContext(Dispatchers.Main) {
                    binding!!.lottieView.visibility = View.VISIBLE
                    currentLottieFile = "$category"
                    binding!!.lottieView.setAnimationFromJson(jsonString)
                    binding!!.lottieView.playAnimation()
                }
            } catch (e: Exception) {
                Log.e("LottieDownload", "Failed to download file: ${e.message}")
                withContext(Dispatchers.Main) {
//                        Toast.makeText(requireContext(), "$e", Toast.LENGTH_SHORT).show()
                }
            }
        }
//        }
    }

    private fun cardViewSlideIn(k: Boolean) {
        if (k) {  // if true & focus has gained
            binding!!.tipCard1.visibility = View.VISIBLE
            slideIn2(binding!!.tipCard1)
            slideIn(binding!!.tipCard2)
            slideIn2(binding!!.tipCard3)
//                Toast.makeText(context, "else is executed", Toast.LENGTH_SHORT).show()

            isTipCardVisible = !isTipCardVisible
        }

    }

    private fun slideIn(cardView: CardView) {
        cardView.visibility = View.VISIBLE

        var startOffset = cardView.width / 1.4f
        // Slide in from left-to-right
        val animate = TranslateAnimation(
            cardView.width.toFloat(),    // Start from this offset
            0f,     // End point (original position)
            0f,
            0f
        )
        animate.duration = 1200 // Animation duration
        animate.fillAfter = true // Keep the view in its final position
        cardView.startAnimation(animate)

//        if(flag == 0){
//            cardView.visibility = View.GONE
////            binding!!.button.performClick()
//        }
    }

    private fun slideIn2(cardView: CardView) {
        cardView.visibility = View.VISIBLE

        var startOffset = cardView.width / 1.4f
        // Slide in from left-to-right
        val animate = TranslateAnimation(
            -cardView.width.toFloat(),    // Start from this offset
            0f,     // End point (original position)
            0f,
            0f
        )
        animate.duration = 1200 // Animation duration
        animate.fillAfter = true // Keep the view in its final position
        cardView.startAnimation(animate)


    }

    private fun slideOut(cardView: CardView) {
        var endOffset = cardView.width / 2f
        // Slide out from right-to-left
        val animate = TranslateAnimation(
            0f,  // Start point (original position)
            cardView.width.toFloat(),  // End point (off-screen to the left)
            0f,
            0f
        )
        animate.duration = 1200 // Animation duration
        animate.fillAfter = false // Reset the view's position after animation
        animate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                cardView.visibility = View.GONE // Set visibility to GONE after the animation ends
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        cardView.startAnimation(animate)
    }

    private fun slideOut2(cardView: CardView) {
        var endOffset = cardView.width / 2f
        // Slide out from right-to-left
        val animate = TranslateAnimation(
            0f,  // Start point (original position)
            -cardView.width.toFloat(),  // End point (off-screen to the left)
            0f,
            0f
        )
        animate.duration = 1200 // Animation duration
        animate.fillAfter = false // Reset the view's position after animation
        animate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                cardView.visibility = View.GONE // Set visibility to GONE after the animation ends
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        cardView.startAnimation(animate)
    }

    // Instant slide-out without animation
    private fun genericMethodToTakeAnimationOneStepAhead(cardView: CardView) {
        cardView.clearAnimation() // Clear any ongoing animation
        cardView.visibility = View.GONE // Directly set visibility to GONE
    }

    // Function to hide keyboard and execute your logic
    private fun hideKeyboardAndExecuteLogic(view: View) {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        view.clearFocus() // Clear focus to prevent reopening keyboard

        executeMyCustomCode()
    }

    // Your dedicated code to execute
    private fun executeMyCustomCode() {
        val s = binding!!.editTextProductName.text
        val text = s.toString()

        if (text.contains("(") && text.contains(")")) {

            // Check if we have the brackets: if no, then go --> to our Logic
            // If have brackets, but not category matched go --> to our logic

            // Continuously checks and performs lottie task
            var category = text.substringAfterLast("(", "").substringBeforeLast(")")
//                    Toast.makeText(requireContext(), "$category", Toast.LENGTH_SHORT).show()

            when (category) {
                "Smartphones" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Fashion Watches" -> {
                    // Perform action for Smartphones\

                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
//                            Toast.makeText(requireContext(), "$category", Toast.LENGTH_SHORT).show()
                    loadLottieAnimation(category)
                }

                "Apple MacBook Variants" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Fashion Shoes" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Cameras" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Fashion Sunglasses" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Musical Instruments" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Vehicles" -> {
                    // Perform action for Smartphones
                    category = category.trim().lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Home Appliances" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Books" -> {
                    // Perform action for Books
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Engineering Notes" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Tablets" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Chargers" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Power Banks" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Keyboards" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Calculators" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Backpacks" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Furniture Desks and Chairs" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Mattresses" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Room Decor" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Sports Equipment" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Bicycles" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Printer Accessories" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Stationery (Notebooks, Pens)" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Software Licenses" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Projectors" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Monitors" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Hard Drives" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Flash Drives" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Gaming Consoles" -> {
                    // Perform action for Gaming Consoles
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Board Games" -> {
                    // Perform action for Board Games
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Art Supplies" -> {
                    // Perform action for Art Supplies
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Sewing Machines" -> {
                    // Perform action for Sewing Machines
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Kitchen Appliances" -> {
                    // Perform action for Kitchen Appliances
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Lamps" -> {
                    // Perform action for Lamps
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Clocks" -> {
                    // Perform action for Clocks
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Water Bottles" -> {
                    // Perform action for Water Bottles
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Lab Coats" -> {
                    // Perform action for Lab Coats
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Safety Goggles" -> {
                    // Perform action for Safety Goggles
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Graphing Paper" -> {
                    // Perform action for Graphing Paper
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Lab Equipment & Microscopes" -> {
                    // Perform action for Lab Equipment & Microscopes
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Literature Books" -> {
                    // Perform action for Literature Books
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Fiction Novels" -> {
                    // Perform action for Fiction Novels
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Journals" -> {
                    // Perform action for Journals
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "E-Readers" -> {
                    // Perform action for E-Readers
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Posters (For Dorm/Hostels Rooms, Study Spaces)" -> {
                    // Perform action for Posters
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Planners (For Study, Assignments, Projects)" -> {
                    // Perform action for Planners
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Coffee Makers (For Late-Night Studying & Hustle Mode)" -> {
                    // Perform action for Coffee Makers
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Curtains" -> {
                    // Perform action for Curtains
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Bed Sheets" -> {
                    // Perform action for Bed Sheets
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
//                            Toast.makeText(requireContext(), "$category", Toast.LENGTH_SHORT).show()
                    loadLottieAnimation(category)
                }

                "Pillows" -> {
                    // Perform action for Pillows
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Rugs" -> {
                    // Perform action for Rugs
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Fans" -> {
                    // Perform action for Fans
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Umbrellas" -> {
                    // Perform action for Umbrellas
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Raincoats" -> {
                    // Perform action for Raincoats
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Shoes" -> {
                    // Perform action for Shoes
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Sports Jerseys" -> {
                    // Perform action for Sports Jerseys
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Dumbbells" -> {
                    // Perform action for Dumbbells
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Yoga Mats" -> {
                    // Perform action for Yoga Mats
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Helmets" -> {
                    // Perform action for Helmets
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Skateboards" -> {
                    // Perform action for Skateboards
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Makeup Kits" -> {
                    // Perform action for Makeup Kits
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Hair Dryers" -> {
                    // Perform action for Hair Dryers
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Hair Straighteners" -> {
                    // Perform action for Hair Straighteners
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Wallets" -> {
                    // Perform action for Wallets
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Watches" -> {
                    // Perform action for Watches
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Sunglasses" -> {
                    // Perform action for Sunglasses
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Jewelry" -> {
                    // Perform action for Jewelry
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Flashlights" -> {
                    // Perform action for Flashlights
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Toolkits" -> {
                    // Perform action for Toolkits
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Screwdrivers" -> {
                    // Perform action for Screwdrivers
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Extension Cords" -> {
                    // Perform action for Extension Cords
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Study Lamps" -> {
                    // Perform action for Study Lamps
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Ethernet Cables" -> {
                    // Perform action for Ethernet Cables
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "USB" -> {
                    // Perform action for USB
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "HDMI Cables" -> {
                    // Perform action for HDMI Cables
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Headphone Splitters" -> {
                    // Perform action for Headphone Splitters
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Bookshelves" -> {
                    // Perform action for Bookshelves
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Whiteboards" -> {
                    // Perform action for Whiteboards
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Markers" -> {
                    // Perform action for Markers
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Clipboards" -> {
                    // Perform action for Clipboards
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "T-Shirts" -> {
                    // Perform action for T-Shirts
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Hoodies" -> {
                    // Perform action for Hoodies
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Jeans" -> {
                    // Perform action for Smartphones
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Dresses" -> {
                    // Perform action for Dresses
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Coats" -> {
                    // Perform action for Coats
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Gloves" -> {
                    // Perform action for Gloves
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Caps" -> {
                    // Perform action for Caps
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Blankets" -> {
                    // Perform action for Blankets
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Crockery" -> {
                    // Perform action for Crockery
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Water Filters" -> {
                    // Perform action for Water Fileter
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Storage Boxes" -> {
                    // Perform action for Storage Boxes
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Tupperware" -> {
                    // Perform action for Tupperware
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Power Strips" -> {
                    // Perform action for Power Strips
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Smart Speakers" -> {
                    // Perform action for Smart Speakers
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "VR Headsets" -> {
                    // Perform action for VR Headsets
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Action Cameras" -> {
                    // Perform action for Action Cameras
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                "Fitness Bands" -> {
                    // Perform action for Fitness Bands
                    category = category.lowercase()
                    category = category.replace("\\s".toRegex(), "")
                    loadLottieAnimation(category)
                }

                else -> {
                    // Perform action for unknown category
                    logicForLoadingLottieIfUserEnterNoPreDefinedInputs(text)
                }
            }
        } else {
            // Our Logic
            logicForLoadingLottieIfUserEnterNoPreDefinedInputs(text)
        }
    }


}