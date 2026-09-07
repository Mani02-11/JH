package com.project.jh.ui.screens

import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.jh.R
import com.project.jh.ui.components.JHButton
import com.project.jh.ui.theme.JHPrimary
import com.project.jh.ui.theme.JHSpaceBlack

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    val context = LocalContext.current
    
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    fun handleAuth() {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!isLoginMode && (name.isBlank() || department.isBlank() || phone.isBlank())) {
            Toast.makeText(context, "Please fill in all registration fields", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        if (isLoginMode) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val db = FirebaseFirestore.getInstance()
                        val userData = hashMapOf(
                            "uid" to user?.uid,
                            "name" to name,
                            "email" to email,
                            "department" to department,
                            "phone" to phone,
                            "rating" to 0.0
                        )

                        user?.uid?.let { uid ->
                            db.collection("users").document(uid).set(userData)
                                .addOnCompleteListener { firestoreTask ->
                                    val profileUpdates = UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build()
                                    user.updateProfile(profileUpdates)
                                        .addOnCompleteListener { profileTask ->
                                            isLoading = false
                                            onLoginSuccess()
                                        }
                                }
                        }
                    } else {
                        isLoading = false
                        Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ui),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Dark overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            
            Text(
                text = "JustHire",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Glassmorphism Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        RoundedCornerShape(32.dp)
                    ),
                shape = RoundedCornerShape(32.dp),
                color = Color.White.copy(alpha = 0.05f),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    if (!isLoginMode) {
                        AuthLabel("Full Name")
                        AuthTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "Enter your name"
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        AuthLabel("Department")
                        AuthTextField(
                            value = department,
                            onValueChange = { department = it },
                            placeholder = "e.g. Computer Science"
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        AuthLabel("Phone Number")
                        AuthTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            placeholder = "+91 XXXXXXXXXX"
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    AuthLabel("Email address")
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "example@gmail.com"
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    AuthLabel("Password")
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("••••••••••••••", color = Color.White.copy(alpha = 0.4f)) },
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JHPrimary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    
                    if (isLoginMode) {
                        TextButton(
                            onClick = { },
                            modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                        ) {
                            Text("Forget Password ?", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = JHPrimary
                        )
                    } else {
                        JHButton(
                            text = if (isLoginMode) "Login" else "Register",
                            onClick = { handleAuth() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isLoginMode) "Are You New Member ?" else "Already have an account ?",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        TextButton(onClick = { isLoginMode = !isLoginMode }) {
                            Text(
                                text = if (isLoginMode) "Sign UP" else "Login",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthLabel(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.8f),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.4f)) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = JHPrimary,
            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        singleLine = true
    )
}
