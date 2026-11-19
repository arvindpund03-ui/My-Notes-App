package com.example.mynoteapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import kotlinx.coroutines.launch
import com.example.mynoteapp.ui.theme.MyNoteAppTheme

private val Purple = Color(0xFF6750A4)
private val LightBackground = Color(0xFFF7F5FF)
private val CardColor = Color.White

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(
            applicationContext,
            NoteDatabase::class.java,
            "my_notes_database"
        ).build()

        setContent {
            MyNoteAppTheme {
                NotesApp(database.noteDao())
            }
        }
    }
}

@Composable
fun NotesApp(dao: NoteDao) {

    val notes by dao.getAllNotes()
        .collectAsState(initial = emptyList())

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    var editingId by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()

    val filteredNotes = notes.filter {
        it.title.contains(search, true) ||
                it.content.contains(search, true)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBackground
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            Text(
                text = "My Notes 📝",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Purple
            )

            Text(
                text = "Your personal space for ideas",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Purple
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "TOTAL NOTES",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )

                    Text(
                        text = "${notes.size}",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Note Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Write your note here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {

                    if (title.isNotBlank() &&
                        content.isNotBlank()
                    ) {

                        scope.launch {

                            if (editingId == 0) {

                                dao.insertNote(
                                    Note(
                                        title = title.trim(),
                                        content = content.trim()
                                    )
                                )

                            } else {

                                dao.updateNote(
                                    Note(
                                        id = editingId,
                                        title = title.trim(),
                                        content = content.trim()
                                    )
                                )
                            }

                            title = ""
                            content = ""
                            editingId = 0
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple
                )
            ) {

                Text(
                    text = if (editingId == 0)
                        "＋  Save Note"
                    else
                        "✓  Update Note",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("🔍 Search Notes") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Your Notes",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = Purple
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(filteredNotes, key = { it.id }) { note ->

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardColor
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 3.dp
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = note.title,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Purple
                            )

                            Spacer(modifier = Modifier.height(7.dp))

                            Text(
                                text = note.content,
                                fontSize = 15.sp,
                                color = Color.DarkGray
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {

                                OutlinedButton(
                                    onClick = {
                                        title = note.title
                                        content = note.content
                                        editingId = note.id
                                    }
                                ) {
                                    Text("✏️ Edit")
                                }

                                Button(
                                    onClick = {
                                        scope.launch {

                                            dao.deleteNote(note)

                                            if (editingId == note.id) {
                                                title = ""
                                                content = ""
                                                editingId = 0
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD32F2F)
                                    )
                                ) {
                                    Text("🗑 Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}