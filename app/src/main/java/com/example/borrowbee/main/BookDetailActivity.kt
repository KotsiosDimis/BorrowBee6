package com.example.borrowbee.main

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Normal
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.borrowbee.R
import com.example.borrowbee.apis.fetchBookDescription
import com.example.borrowbee.data.entities.Book
import com.example.borrowbee.firebase.transactions.rentBook
import com.example.borrowbee.ui.tabs.fixUrl
import com.example.borrowbee.ui.tabs.key_book
import com.example.borrowbee.ui.tabs.key_is_bookmarked
import com.example.borrowbee.ui.theme.robotoCondenseFamily


class BookDetailActivity : AppCompatActivity() {

    lateinit var book: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        book = intent.getParcelableExtra(key_book)!!
        changeStatusBarColor("#FF0000")

        setContent {
            MaterialTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    TopSection()
                    BottomSection()
                }
            }
        }
    }

    private fun changeStatusBarColor(color: String) {
        //window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = android.graphics.Color.parseColor(color)
    }

    @Composable
    fun TopSection() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.onBackground,),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            TopBar()
            BookDetail()
            Spacer(modifier = Modifier.height(18.dp))
        }
    }

    @Composable
    fun TopBar() {
        val iconSize = 50.dp
        val viewModel: MyViewModel = viewModel()

        val isBookmarked = remember { mutableStateOf(
            intent.getBooleanExtra(key_is_bookmarked, false)
        ) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(iconSize)
                    .padding(vertical = 8.dp, horizontal = 6.dp)
                    .clickable { onBackPressedDispatcher.onBackPressed() }
            )

            Icon(
                painter =
                if(isBookmarked.value)
                    painterResource(id = R.drawable.ic_bookmark_filled)
                else
                    painterResource(id = R.drawable.ic_bookmark),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(iconSize)
                    .padding(vertical = 8.dp, horizontal = 6.dp)
                    .clickable {
                        isBookmarked.value = !isBookmarked.value
                        if (isBookmarked.value){
                                viewModel.addToFavUserBooks(1,book.isbn13.toString())
                        }
                        else{
                                viewModel.removeToFavUserBooks(1,book.isbn13.toString())
                        }
                    }
            )
        }
    }

    @Composable
    fun BookDetail() {

        val isbn = book.isbn13

        val (bookDescription, setBookDescription) = remember { mutableStateOf("Loading book description...") }

        fetchBookDescription(isbn.toLong()) { description ->
            if (description != null) {
                // Use the book description
                setBookDescription("$description")
            } else {
                // Handle case where book not found or API request failed
                setBookDescription("Book not found or API request failed")
            }
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(

                model = fixUrl(book.imageUrl), // fixUrls fixes http to https
                placeholder = painterResource(id = R.drawable.book_boss_of_the_body),
                error = painterResource(id = R.drawable.book_eat_better),
                contentDescription = book.title,
                modifier = Modifier
                    .height(280.dp)
                    .width(140.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.FillHeight,

                )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = book.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                fontFamily = robotoCondenseFamily
            )

            Text(
                text = "By ${book.author}",
                color = Color.White,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                fontFamily = robotoCondenseFamily
            )

            Spacer(modifier = Modifier.height(20.dp))

            DetailGrid()
            Spacer(modifier = Modifier.height(32.dp))
            ActionButtons()

        }
    }

    @Composable
    fun DetailGrid() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = R.string.rating),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Light,
                    fontSize = 16.sp,
                    fontFamily = robotoCondenseFamily
                )

                Text(
                    text = "4.7",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = robotoCondenseFamily
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = R.string.pages),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Light,
                    fontSize = 16.sp,
                    fontFamily = robotoCondenseFamily
                )

                Text(
                    text = "155",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = robotoCondenseFamily
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = R.string.language),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Light,
                    fontSize = 16.sp,
                    fontFamily = robotoCondenseFamily
                )

                Text(
                    text = "ENG",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = robotoCondenseFamily
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = R.string.audio),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Light,
                    fontSize = 16.sp,
                    fontFamily = robotoCondenseFamily
                )

                Text(
                    text = "02 Hrs",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = robotoCondenseFamily
                )
            }
        }
    }

    @Composable
    fun ActionButtons() {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {

            ActionButton(
                text = getString(R.string.rent_book), modifier =
                Modifier
                    .background(
                        shape = RoundedCornerShape(topStartPercent = 20, bottomStartPercent = 20),
                        color = colorResource(id = R.color.action_button_background_color)

                    )
                    .clickable( onClick = { rentBook(
                        book.isbn13,
                        "TODAY",
                        "10 days later",
                        "Rent ",
                        "1"
                    ) }

                    ),
                R.drawable.ic_read_book,
            )

            Spacer(modifier = Modifier.width(4.dp))

        }
    }

    @Composable
    fun ActionButton(text: String, modifier: Modifier, icon: Int) {
        val buttonHeight = 50.dp
        val buttonWidth = 170.dp

        Row(
            modifier = modifier
                .height(buttonHeight)
                .width(buttonWidth)
                .clickable {rentBook(
                    book.isbn13,
                    "TODAY222222",
                    "10 days later",
                    "Rent ",
                    "1"
                )},
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = text,
                color = Color.White
            )
        }
    }

    @Composable
    fun BottomSection() {
        val headingFontSize = 22.sp
        val contentFontSize = 18.sp

        // Inside your composable function
        val isbn = book.isbn13
        val (bookDescription, setBookDescription) = remember { mutableStateOf("Loading book description...") }

        fetchBookDescription(isbn.toLong()) { description ->
            if (description != null) {
                // Use the book description
                setBookDescription("Book Description: $description")
            } else {
                // Handle case where book not found or API request failed
                setBookDescription("Book not found or API request failed")
            }
        }

        Box(
            modifier = Modifier
                .wrapContentWidth()
                .background(color = colorResource(id = R.color.background_dark))
        ) {

            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    getString(R.string.what_is_it_about),
                    color = Color.White,
                    fontSize = headingFontSize,
                    fontFamily = robotoCondenseFamily,
                    fontWeight = SemiBold,
                )
                Text(
                    text = bookDescription,
                    color = Color.White,
                    fontSize = contentFontSize,
                    fontFamily = robotoCondenseFamily,
                    fontWeight = Normal,
                )
                Spacer(modifier = Modifier.height(16.dp))

            }
        }


    }
}