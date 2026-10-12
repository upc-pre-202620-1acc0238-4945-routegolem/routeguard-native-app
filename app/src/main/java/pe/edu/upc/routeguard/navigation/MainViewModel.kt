package pe.edu.upc.routeguard.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pe.edu.upc.routeguard.core.session.SessionManager
import pe.edu.upc.routeguard.identityaccessmanagement.application.GetCurrentAccountUseCase
import pe.edu.upc.routeguard.identityaccessmanagement.application.SignOutUseCase
import pe.edu.upc.routeguard.identityaccessmanagement.domain.Account
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getCurrentAccount: GetCurrentAccountUseCase,
    private val signOut: SignOutUseCase,
    sessionManager: SessionManager
) : ViewModel() {

    val account: Account? = getCurrentAccount()

    /** Emits when the server rejects the session token, so the shell can go back to sign in. */
    val sessionRejected = sessionManager.sessionRejected

    fun onSignOut() = signOut()
}
