package com.fit5046.wattwise


// Password validation regex: min 8 chars, 1 uppercase, 1 digit, 1 special char
// Design Guideline 1: Complexity enforcement with real-time keystroke validation
private val PASSWORD_REGEX = Regex("^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#!$%^&*]).{8,}$")

fun validatePassword(password: String): String? {
    return if (password.isEmpty()) null
    else if (!PASSWORD_REGEX.matches(password))
        "Password must be 8+ characters with an uppercase letter, number, and special character"
    else null
}

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: WattWiseViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var showRegister by remember { mutableStateOf(false) }

    if (showRegister) {
        RegisterScreen(
            onRegisterSuccess = {
                showRegister = false
                onLoginSuccess()
            },
            onBackToLogin = { showRegister = false },
            viewModel = viewModel
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF388E3C))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "WattWise Logo",
                tint = Color(0xFF69F0AE),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "WattWise", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = "Smart Home Energy Monitor", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Sign In", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                    Spacer(modifier = Modifier.height(20.dp))

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = if (it.isNotEmpty() && !it.contains("@"))
                                "Please enter a valid email address" else null
                        },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = Color(0xFF2E7D32)) },
                        isError = emailError != null,
                        supportingText = {
                            if (emailError != null) Text(emailError!!, color = MaterialTheme.colorScheme.error)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2E7D32),
                            focusedLabelColor = Color(0xFF2E7D32)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Password field with show/hide toggle
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = validatePassword(it)
                        },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password", tint = Color(0xFF2E7D32)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = Color(0xFF2E7D32)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordError != null,
                        supportingText = {
                            if (passwordError != null) Text(passwordError!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2E7D32),
                            focusedLabelColor = Color(0xFF2E7D32)
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    // Sign In Button
                    Button(
                        onClick = {
                            val pErr = validatePassword(password)
                            if (email.isNotEmpty() && pErr == null && password.isNotEmpty()) {
                                viewModel.fullName = email.substringBefore("@")
                                onLoginSuccess()
                            } else {
                                if (email.isEmpty()) emailError = "Email is required"
                                if (password.isEmpty()) passwordError = "Password is required"
                                else passwordError = pErr
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))