package pe.edu.upc.routeguard.stakeholderassetmanagement.presentation.registration

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.designsystem.components.InfoCard
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.AccountCredentials

/** Shows the credentials generated automatically for a new driver or parent account. */
@Composable
fun CredentialsCard(credentials: AccountCredentials, modifier: Modifier = Modifier) {
    InfoCard(
        title = stringResource(R.string.credentials_title),
        subtitle = stringResource(R.string.credentials_subtitle),
        modifier = modifier
    ) {
        Text(text = stringResource(R.string.credentials_username, credentials.email))
        Text(text = stringResource(R.string.credentials_password, credentials.temporaryPassword), fontWeight = FontWeight.Bold)
    }
}
