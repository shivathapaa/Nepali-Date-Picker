/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.shivathapaa.nepalidatepickerkmp.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal object NepaliIcons {

    val KeyboardArrowLeft: ImageVector
        get() {
            _keyboardArrowLeft?.let { return it }
            return ImageVector.Builder(
                name = "KeyboardArrowLeft",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
                autoMirror = true,
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Bevel,
                    strokeLineMiter = 1f,
                    pathFillType = PathFillType.NonZero,
                ) {
                    moveTo(10.8f, 12f)
                    lineToRelative(3.9f, 3.9f)
                    quadToRelative(0.28f, 0.28f, 0.28f, 0.7f)
                    quadToRelative(0f, 0.42f, -0.28f, 0.7f)
                    reflectiveQuadTo(14f, 17.58f)
                    reflectiveQuadTo(13.3f, 17.3f)
                    lineTo(8.7f, 12.7f)
                    quadTo(8.55f, 12.55f, 8.49f, 12.38f)
                    reflectiveQuadTo(8.43f, 12f)
                    reflectiveQuadTo(8.49f, 11.63f)
                    reflectiveQuadTo(8.7f, 11.3f)
                    lineTo(13.3f, 6.7f)
                    quadTo(13.58f, 6.43f, 14f, 6.43f)
                    reflectiveQuadTo(14.7f, 6.7f)
                    reflectiveQuadToRelative(0.28f, 0.7f)
                    reflectiveQuadTo(14.7f, 8.1f)
                    lineTo(10.8f, 12f)
                    close()
                }
            }.build().also { _keyboardArrowLeft = it }
        }

    val KeyboardArrowRight: ImageVector
        get() {
            _keyboardArrowRight?.let { return it }
            return ImageVector.Builder(
                name = "KeyboardArrowRight",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
                autoMirror = true,
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Bevel,
                    strokeLineMiter = 1f,
                    pathFillType = PathFillType.NonZero,
                ) {
                    moveTo(12.6f, 12f)
                    lineTo(8.7f, 8.1f)
                    quadTo(8.43f, 7.82f, 8.43f, 7.4f)
                    reflectiveQuadTo(8.7f, 6.7f)
                    reflectiveQuadTo(9.4f, 6.43f)
                    reflectiveQuadTo(10.1f, 6.7f)
                    lineToRelative(4.6f, 4.6f)
                    quadToRelative(0.15f, 0.15f, 0.21f, 0.33f)
                    reflectiveQuadTo(14.98f, 12f)
                    reflectiveQuadToRelative(-0.06f, 0.38f)
                    reflectiveQuadTo(14.7f, 12.7f)
                    lineToRelative(-4.6f, 4.6f)
                    quadTo(9.83f, 17.58f, 9.4f, 17.58f)
                    reflectiveQuadTo(8.7f, 17.3f)
                    quadTo(8.43f, 17.02f, 8.43f, 16.6f)
                    reflectiveQuadTo(8.7f, 15.9f)
                    lineTo(12.6f, 12f)
                    close()
                }
            }.build().also { _keyboardArrowRight = it }
        }

    val ArrowDropDown: ImageVector
        get() {
            _arrowDropDown?.let { return it }
            return ImageVector.Builder(
                name = "ArrowDropDown",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Bevel,
                    strokeLineMiter = 1f,
                    pathFillType = PathFillType.NonZero,
                ) {
                    moveTo(11.48f, 14.48f)
                    lineTo(7.85f, 10.85f)
                    quadTo(7.78f, 10.77f, 7.74f, 10.69f)
                    reflectiveQuadTo(7.7f, 10.5f)
                    quadToRelative(0f, -0.2f, 0.14f, -0.35f)
                    reflectiveQuadTo(8.2f, 10f)
                    horizontalLineToRelative(7.6f)
                    quadToRelative(0.23f, 0f, 0.36f, 0.15f)
                    reflectiveQuadTo(16.3f, 10.5f)
                    quadToRelative(0f, 0.05f, -0.15f, 0.35f)
                    lineToRelative(-3.63f, 3.63f)
                    quadTo(12.4f, 14.6f, 12.28f, 14.65f)
                    reflectiveQuadTo(12f, 14.7f)
                    reflectiveQuadTo(11.73f, 14.65f)
                    reflectiveQuadTo(11.48f, 14.48f)
                    close()
                }
            }.build().also { _arrowDropDown = it }
        }

    val DateRange: ImageVector
        get() {
            _dateRange?.let { return it }
            return ImageVector.Builder(
                name = "DateRange",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Bevel,
                    strokeLineMiter = 1f,
                    pathFillType = PathFillType.NonZero,
                ) {
                    moveTo(7.29f, 13.71f)
                    quadTo(7f, 13.43f, 7f, 13f)
                    reflectiveQuadTo(7.29f, 12.29f)
                    reflectiveQuadTo(8f, 12f)
                    reflectiveQuadToRelative(0.71f, 0.29f)
                    reflectiveQuadTo(9f, 13f)
                    reflectiveQuadTo(8.71f, 13.71f)
                    reflectiveQuadTo(8f, 14f)
                    quadTo(7.58f, 14f, 7.29f, 13.71f)
                    close()
                    moveToRelative(4f, 0f)
                    quadTo(11f, 13.43f, 11f, 13f)
                    reflectiveQuadToRelative(0.29f, -0.71f)
                    reflectiveQuadTo(12f, 12f)
                    reflectiveQuadToRelative(0.71f, 0.29f)
                    reflectiveQuadTo(13f, 13f)
                    reflectiveQuadToRelative(-0.29f, 0.71f)
                    reflectiveQuadTo(12f, 14f)
                    reflectiveQuadTo(11.29f, 13.71f)
                    close()
                    moveToRelative(4f, 0f)
                    quadTo(15f, 13.43f, 15f, 13f)
                    reflectiveQuadToRelative(0.29f, -0.71f)
                    reflectiveQuadTo(16f, 12f)
                    quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                    reflectiveQuadTo(17f, 13f)
                    reflectiveQuadToRelative(-0.29f, 0.71f)
                    reflectiveQuadTo(16f, 14f)
                    reflectiveQuadTo(15.29f, 13.71f)
                    close()
                    moveTo(5f, 22f)
                    quadTo(4.18f, 22f, 3.59f, 21.41f)
                    reflectiveQuadTo(3f, 20f)
                    verticalLineTo(6f)
                    quadTo(3f, 5.18f, 3.59f, 4.59f)
                    reflectiveQuadTo(5f, 4f)
                    horizontalLineTo(6f)
                    verticalLineTo(3f)
                    quadTo(6f, 2.57f, 6.29f, 2.29f)
                    reflectiveQuadTo(7f, 2f)
                    reflectiveQuadTo(7.71f, 2.29f)
                    reflectiveQuadTo(8f, 3f)
                    verticalLineTo(4f)
                    horizontalLineToRelative(8f)
                    verticalLineTo(3f)
                    quadTo(16f, 2.57f, 16.29f, 2.29f)
                    reflectiveQuadTo(17f, 2f)
                    reflectiveQuadToRelative(0.71f, 0.29f)
                    reflectiveQuadTo(18f, 3f)
                    verticalLineTo(4f)
                    horizontalLineToRelative(1f)
                    quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                    quadTo(21f, 5.18f, 21f, 6f)
                    verticalLineTo(20f)
                    quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                    reflectiveQuadTo(19f, 22f)
                    horizontalLineTo(5f)
                    close()
                    moveTo(5f, 20f)
                    horizontalLineTo(19f)
                    verticalLineTo(10f)
                    horizontalLineTo(5f)
                    verticalLineTo(20f)
                    close()
                    moveTo(5f, 8f)
                    horizontalLineTo(19f)
                    verticalLineTo(6f)
                    horizontalLineTo(5f)
                    verticalLineTo(8f)
                    close()
                }
            }.build().also { _dateRange = it }
        }

    val Edit: ImageVector
        get() {
            _edit?.let { return it }
            return ImageVector.Builder(
                name = "Edit",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Bevel,
                    strokeLineMiter = 1f,
                    pathFillType = PathFillType.NonZero,
                ) {
                    moveTo(5f, 19f)
                    horizontalLineTo(6.43f)
                    lineTo(16.2f, 9.23f)
                    lineTo(14.78f, 7.8f)
                    lineTo(5f, 17.58f)
                    verticalLineTo(19f)
                    close()
                    moveTo(4f, 21f)
                    quadTo(3.58f, 21f, 3.29f, 20.71f)
                    quadTo(3f, 20.43f, 3f, 20f)
                    verticalLineTo(17.58f)
                    quadToRelative(0f, -0.4f, 0.15f, -0.76f)
                    reflectiveQuadTo(3.58f, 16.18f)
                    lineTo(16.2f, 3.57f)
                    quadTo(16.5f, 3.3f, 16.86f, 3.15f)
                    reflectiveQuadTo(17.63f, 3f)
                    quadToRelative(0.4f, 0f, 0.78f, 0.15f)
                    reflectiveQuadTo(19.05f, 3.6f)
                    lineTo(20.43f, 5f)
                    quadToRelative(0.3f, 0.27f, 0.44f, 0.65f)
                    reflectiveQuadTo(21f, 6.4f)
                    quadToRelative(0f, 0.4f, -0.14f, 0.76f)
                    reflectiveQuadTo(20.43f, 7.82f)
                    lineTo(7.83f, 20.43f)
                    quadTo(7.55f, 20.7f, 7.19f, 20.85f)
                    quadTo(6.83f, 21f, 6.43f, 21f)
                    horizontalLineTo(4f)
                    close()
                }
            }.build().also { _edit = it }
        }

    private var _keyboardArrowLeft: ImageVector? = null
    private var _keyboardArrowRight: ImageVector? = null
    private var _arrowDropDown: ImageVector? = null
    private var _dateRange: ImageVector? = null
    private var _edit: ImageVector? = null
}
