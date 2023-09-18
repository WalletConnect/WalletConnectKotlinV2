package com.walletconnect.sample.common.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.walletconnect.sample.common.BuildConfig
import com.walletconnect.sample.common.R

@Composable
fun WCTopAppBar(
    modifier: Modifier = Modifier,
    titleText: String,
    versionText: String = BuildConfig.BOM_VERSION,
    titleStyle: TextStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        color = themedColor(darkColor = 0xFFE5E7E7, lightColor = 0xFF141414)
    ),
    versionStyle: TextStyle = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = 11.sp,
        color = themedColor(darkColor = 0xFFE5E7E7, lightColor = 0xFF141414)
    ),
    @DrawableRes icon: Int? = null,
    onIconClick: (() -> Unit)? = null,
    onBackIconClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        onBackIconClick?.let {
            Icon(
                tint = Color(0xFF3496ff),
                imageVector = ImageVector.vectorResource(id = R.drawable.chevron_left),
                contentDescription = "BackArrow",
                modifier = Modifier.clickable { onBackIconClick() }
            )
            Spacer(modifier = Modifier.width(32.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(text = titleText, style = titleStyle)
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.End,
            ) {
                icon?.let {
                    Icon(
                        tint = Color(0xFF3496ff),
                        imageVector = ImageVector.vectorResource(id = icon),
                        contentDescription = "Icon",
                        modifier = Modifier.clickable { onIconClick?.invoke() }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Text(text = versionText, style = versionStyle)
            }
        }
    }
}

@Composable
fun WCTopAppBar2(
    titleText: String,
    @DrawableRes firstIcon: Int? = null,
    @DrawableRes secondIcon: Int? = null,
    onFirstIconClick: (() -> Unit)? = null,
    onSecondIconClick: (() -> Unit)? = null,
    onBackIconClick: (() -> Unit)? = null,
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(30.dp))
    ) {
        val startGuideline = createGuidelineFromStart(10.dp)

        val (
            backIconRef,
            titleRef,
            firstIconRef,
            secondIconRef,
        ) = createRefs()

        onBackIconClick?.let {
            Icon(
                modifier = Modifier
                    .constrainAs(backIconRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(startGuideline)
                        width = Dimension.wrapContent
                        height = Dimension.fillToConstraints
                    }
                    .clickable { onBackIconClick() },
                tint = Color(0xFF3496ff),
                imageVector = ImageVector.vectorResource(id = R.drawable.chevron_left),
                contentDescription = "BackArrow",
            )
        }

        Text(
            modifier = Modifier
                .constrainAs(titleRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(backIconRef.end, 8.dp)
                    width = Dimension.wrapContent
                    height = Dimension.wrapContent
                    verticalChainWeight = .5f
                },
            text = titleText,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                color = themedColor(darkColor = 0xFFE5E7E7, lightColor = 0xFF141414)
            )
        )

        firstIcon?.let {
            Image(
                modifier = Modifier
                    .constrainAs(firstIconRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(secondIconRef.start, 8.dp)
                        width = Dimension.wrapContent
                        height = Dimension.wrapContent
                        verticalChainWeight = .5f
                    }
                    .clickable { onFirstIconClick?.invoke() },
                painter = painterResource(id = firstIcon),
                contentDescription = "Icon",
            )
        }

        secondIcon?.let {
            Image(
                modifier = Modifier
                    .constrainAs(secondIconRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, 8.dp)
                        width = Dimension.wrapContent
                        height = Dimension.wrapContent
                        verticalChainWeight = .5f
                    }
                    .clickable { onSecondIconClick?.invoke() },
                painter = painterResource(id = secondIcon),
                contentDescription = "Icon",
            )
        }
    }
}