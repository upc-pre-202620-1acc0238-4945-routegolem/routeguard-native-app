package pe.edu.upc.routeguard

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import pe.edu.upc.routeguard.core.designsystem.theme.RouteGuardTheme
import pe.edu.upc.routeguard.core.session.SessionManager
import pe.edu.upc.routeguard.features.iam.presentation.navigation.IamNavGraphRoute
import pe.edu.upc.routeguard.navigation.AppNavHost
import pe.edu.upc.routeguard.navigation.MainShellRoute
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestLocalNetworkAccess()

        val startDestination: Any =
            if (sessionManager.current() != null) MainShellRoute else IamNavGraphRoute

        setContent {
            val navController = rememberNavController()
            RouteGuardTheme(dynamicColor = false) {
                // Paints the theme background so dark mode does not show the light window color
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavHost(navController, startDestination)
                }
            }
        }
    }

    /** Android 17 blocks apps from reaching local-network hosts (e.g. the backend at 10.0.2.2) without this permission. */
    private fun requestLocalNetworkAccess() {
        if (Build.VERSION.SDK_INT < 37) return
        val permission = "android.permission.ACCESS_LOCAL_NETWORK"
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) return
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }.launch(permission)
    }
}
