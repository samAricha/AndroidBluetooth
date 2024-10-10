package com.elementalist.enose.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionScreen(viewModel: MainViewModel) {
    val imageShown = viewModel.image
    val connectionState = viewModel.connectionState
    val lockedWeight = viewModel.lockedWeight
    val currentWeight = viewModel.currentWeight
    val isWeightLocked = viewModel.isWeightLocked


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
            .border(5.dp, MaterialTheme.colors.secondary)
            .padding(5.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // Dynamically display the current weight being received
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (isWeightLocked) {
                // Display locked weight when it is locked in
                Text(
                    text = "Locked Weight: $lockedWeight kg",
                    style = MaterialTheme.typography.h4,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                // Show dynamic weight until it's locked
                Text(
                    text = if (currentWeight != null) "Current Weight: $currentWeight kg" else "Waiting for weight...",
                    style = MaterialTheme.typography.h4,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }


        //Loading / Error / OK-Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(6f, true)
                .align(Alignment.CenterHorizontally)
        ) {
            if (isWeightLocked){
                Image(
                    painter = painterResource(imageShown),
                    contentDescription = "Result from sniffing",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.Center)
                )
            }else{
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }



            //We hide the image-result when it is not needed
           /* when (connectionState) {
                StatesOfConnectionEnum.CLIENT_STARTED, StatesOfConnectionEnum.WEIGHT_LOCKED -> {
                    Image(
                        painter = painterResource(imageShown),
                        contentDescription = "Result from sniffing",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .size(200.dp)
                            .align(Alignment.Center)
                    )
                }
                StatesOfConnectionEnum.RECEIVING_RESPONSE -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center)
                    )
                }
                else -> {
                    //on error leave blank
                }
            } */

        }

        // Button for re-listening for data after a result
        if (isWeightLocked) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(50.dp),
                onClick = { viewModel.restartWeighing() }
            ) {
                Text(text = "Restart")
            }
        }

        // Lock Weight Button at the bottom of the screen
        if (!isWeightLocked) {
            Button(
                onClick = { viewModel.lockInWeight() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(50.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Lock Weight")
            }
        }

        // Once the weight is locked, show a "Done" message
        if (isWeightLocked) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Weight locked. Reading complete!",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

}
