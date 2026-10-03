// Add this to MainActivity onCreate()
// BEFORE setupRecyclerView()

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding      = ActivityMainBinding.inflate(layoutInflater)
    quotaPrefs   = QuotaPreferences(this)
    setContentView(binding.root)

    // 🔑 CHECK IF KEY EXISTS FIRST!
    if (!quotaPrefs.hasApiKey()) {
        // No key yet → Go to setup screen
        startActivity(Intent(this, ApiKeyActivity::class.java))
        finish()
        return
    }

    // ✅ Key exists → Set it in ApiClient
    ApiClient.setApiKey(quotaPrefs.getApiKey())

    // 🚀 Continue normal setup
    setupRecyclerView()
    setupRefreshButton()
    observeViewModel()

    // ⚙️ Add settings icon to change key later
    binding.btnChangeKey.setOnClickListener {
        startActivity(Intent(this, ApiKeyActivity::class.java))
    }
}
