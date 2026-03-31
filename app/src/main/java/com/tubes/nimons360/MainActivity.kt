class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // token expiry broadcast receiver
    private val tokenExpiredReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            (application as NimonsApplication).tokenManager.clearToken()
            navController.navigate(R.id.loginFragment,
                null, NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // check login state on startup
        val app = application as NimonsApplication
        if (!app.tokenManager.isLoggedIn()) {
            navController.navigate(R.id.loginFragment,
                null, NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build())
        }

        setupBottomNav()
        setupHeader()
        setupFab()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            tokenExpiredReceiver, IntentFilter("com.tubes.nimons360.ACTION_TOKEN_EXPIRED"))

        observeNetworkStatus()
    }

    private fun setupBottomNav() {
        binding.bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, dest, _ ->
            val showNavPages = setOf(R.id.homeFragment, R.id.mapFragment, R.id.familiesFragment)
            val isNavPage = dest.id in showNavPages
            binding.bottomNav.visibility = if (isNavPage) View.VISIBLE else View.GONE
            binding.toolbar.visibility = if (isNavPage) View.VISIBLE else View.GONE
            binding.fabCreateFamily.visibility = if (isNavPage) View.VISIBLE else View.GONE
            // update toolbar title
            binding.toolbar.title = dest.label?.toString() ?: ""
        }
    }

    private fun setupHeader() {
        binding.avatarButton.setOnClickListener {
            navController.navigate(R.id.profileFragment)
        }
    }

    private fun setupFab() {
        binding.fabCreateFamily.setOnClickListener {
            navController.navigate(R.id.createFamilyFragment)
        }
    }

    private fun observeNetworkStatus() {
        val app = application as NimonsApplication
        app.networkMonitor.isOnline.observe(this) { isOnline ->
            if (!isOnline) showNoInternetDialog() else dismissNoInternetDialog()
        }
    }

    private var noInternetDialog: AlertDialog? = null
    private fun showNoInternetDialog() {
        if (noInternetDialog?.isShowing == true) return
        noInternetDialog = MaterialAlertDialogBuilder(this)
            .setTitle("No Internet Connection")
            .setMessage("You are currently offline. Some features may not work.")
            .setCancelable(false)
            .show()
    }
    private fun dismissNoInternetDialog() {
        noInternetDialog?.dismiss()
        noInternetDialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(tokenExpiredReceiver)
    }
}