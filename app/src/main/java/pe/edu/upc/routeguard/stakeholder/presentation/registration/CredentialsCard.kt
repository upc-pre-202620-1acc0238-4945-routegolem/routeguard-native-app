package pe.edu.upc.routeguard.stakeholder.presentation.registration

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import pe.edu.upc.routeguard.core.designsystem.components.InfoCard
import pe.edu.upc.routeguard.stakeholder.domain.AccountCredentials

/** Shows the credentials generated automatically for a new driver or parent account. */
@Composable
fun CredentialsCard(credentials: AccountCredentials, modifier: Modifier = Modifier) {
    InfoCard(
        title = "Cuenta creada",
        subtitle = "Entrega estas credenciales a la persona registrada.",
        modifier = modifier
    ) {
        Text(text = "Usuario: ${credentials.email}")
        Text(text = "Contraseña: ${credentials.temporaryPassword}", fontWeight = FontWeight.Bold)
    }
}
