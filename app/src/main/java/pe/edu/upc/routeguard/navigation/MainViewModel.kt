package pe.edu.upc.routeguard.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pe.edu.upc.routeguard.features.iam.application.GetCurrentAccountUseCase
import pe.edu.upc.routeguard.features.iam.application.SignOutUseCase
import pe.edu.upc.routeguard.features.iam.domain.Account
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getCurrentAccount: GetCurrentAccountUseCase,
    private val signOut: SignOutUseCase
) : ViewModel() {

    val account: Account? = getCurrentAccount()

    fun onSignOut() = signOut()
}
