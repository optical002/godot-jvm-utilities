package io.github.optical002.godot.parser

import io.github.optical002.godot.parser.model.*
import io.github.optical002.godot.parser.core.*
import munit.*

class FullTscnSpec extends FunSuite {
  private val tscnFirst =
    """
      |[gd_scene load_steps=8 format=3 uid="uid://d3doqyggcpkeb"]
      |
      |[ext_resource type="Script" path="res://Player.cs" id="1_8162q"]
      |[ext_resource type="Texture2D" uid="uid://bxacee62lu81" path="res://art/playerGrey_up1.png" id="1_d8csi"]
      |[ext_resource type="Texture2D" uid="uid://b70twminywsyj" path="res://art/playerGrey_up2.png" id="2_ljnug"]
      |[ext_resource type="Texture2D" uid="uid://81wtq6p1bwfg" path="res://art/playerGrey_walk1.png" id="3_krmrv"]
      |[ext_resource type="Texture2D" uid="uid://da45skrrq48dj" path="res://art/playerGrey_walk2.png" id="4_jrmwk"]
      |
      |[sub_resource type="SpriteFrames" id="SpriteFrames_707dc"]
      |animations = [{
      |"frames": [
      |{
      |"duration": 1.0,
      |"texture": ObjectValue.ExtResource("1_d8csi")
      |},
      |{
      |"duration": 1.0,
      |"texture": ObjectValue.ExtResource("2_ljnug")
      |}
      |],
      |"loop": true,
      |"name": &"up",
      |"speed": 5.0
      |}, {
      |"frames": [{
      |"duration": 1.0,
      |"texture": ObjectValue.ExtResource("3_krmrv")
      |}, {
      |"duration": 1.0,
      |"texture": ObjectValue.ExtResource("4_jrmwk")
      |}],
      |"loop": true,
      |"name": &"walk",
      |"speed": 5.0
      |}]
      |
      |[sub_resource type="CapsuleShape2D" id="CapsuleShape2D_6tr4r"]
      |radius = 27.0
      |height = 70.0
      |
      |[node name="Player" type="Area2D"]
      |script = ObjectValue.ExtResource("1_8162q")
      |metadata/_edit_group_ = true
      |
      |[node name="AnimatedSprite2d" type="AnimatedSprite2D" parent="."]
      |scale = Vector2(0.5, 0.5)
      |sprite_frames = SubResource("SpriteFrames_707dc")
      |animation = &"up"
      |
      |[node name="CollisionShape2d" type="CollisionShape2D" parent="."]
      |shape = SubResource("CapsuleShape2D_6tr4r")
      |
      |[connection signal="body_entered" from="." to="." method="_on_body_entered"]
      |""".stripMargin

  private val tscnSecond =
    """
      |[gd_scene load_steps=10 format=3 uid="uid://buj8wisq0l07p"]
      |
      |[ext_resource type="Script" path="res://Mob.cs" id="1_k5y70"]
      |[ext_resource type="Texture2D" uid="uid://bye51w2ru5vie" path="res://art/enemyFlyingAlt_1.png" id="1_ytrj5"]
      |[ext_resource type="Texture2D" uid="uid://dhfn0d6qh0qvy" path="res://art/enemyFlyingAlt_2.png" id="2_bls2m"]
      |[ext_resource type="Texture2D" uid="uid://bwrqjt5jq5xfb" path="res://art/enemySwimming_1.png" id="3_j0cqp"]
      |[ext_resource type="Texture2D" uid="uid://c5uy3836dmlt5" path="res://art/enemySwimming_2.png" id="4_bnymk"]
      |[ext_resource type="Texture2D" uid="uid://cdv0chn06a4bm" path="res://art/enemyWalking_1.png" id="5_kl1oy"]
      |[ext_resource type="Texture2D" uid="uid://cr0p08wnu2xm8" path="res://art/enemyWalking_2.png" id="6_h88k2"]
      |
      |[sub_resource type="SpriteFrames" id="SpriteFrames_7jrot"]
      |animations = [{
      |"frames": [{
      |"duration": 1.0,
      |"texture": ObjectValue.ExtResource("1_ytrj5")
      |}, {
      |"duration": 1.0,
      |"texture": ObjectValue.ExtResource("2_bls2m")
      |}],
      |"loop": true,
      |"name": &"fly",
      |"speed": 3.0
      |}, {
      |"frames": [{
      |"duration": 1.0,
      |"texture": ObjectValue.ExtResource("3_j0cqp")
      |}, {
      |"duration": 1.0,
      |"texture": ObjectValue.ExtResource("4_bnymk")
      |}],
      |"loop": true,
      |"name": &"swim",
      |"speed": 3.0
      |}, {
      |"frames": [{
      |"duration": 1.0,
      |"texture": ObjectValue.ExtResource("5_kl1oy")
      |}, {
      |"duration": 1.0,
      |"texture": ObjectValue.ExtResource("6_h88k2")
      |}],
      |"loop": true,
      |"name": &"walk",
      |"speed": 3.0
      |}]
      |
      |[sub_resource type="CapsuleShape2D" id="CapsuleShape2D_r7hl5"]
      |radius = 58.0
      |height = 120.0
      |
      |[node name="Mob" type="RigidBody2D"]
      |collision_mask = 0
      |gravity_scale = 0.0
      |script = ObjectValue.ExtResource("1_k5y70")
      |metadata/_edit_group_ = true
      |
      |[node name="AnimatedSprite2D" type="AnimatedSprite2D" parent="."]
      |scale = Vector2(0.75, 0.75)
      |sprite_frames = SubResource("SpriteFrames_7jrot")
      |animation = &"swim"
      |
      |[node name="VisibleOnScreenNotifier2D" type="VisibleOnScreenNotifier2D" parent="."]
      |
      |[node name="CollisionShape2D" type="CollisionShape2D" parent="."]
      |rotation = 1.5708
      |shape = SubResource("CapsuleShape2D_r7hl5")
      |
      |[connection signal="screen_exited" from="VisibleOnScreenNotifier2D" to="." method="OnVisibleOnScreenNotifier2DScreenExited"]
      |""".stripMargin

  private val tscnThird =
    """
      |[gd_scene load_steps=8 format=3 uid="uid://hc1rholre01q"]
      |
      |[ext_resource type="Script" path="res://Main.cs" id="1_4hmc7"]
      |[ext_resource type="PackedScene" uid="uid://05vlkouevqb2" path="res://Mob.tscn" id="2_q46v7"]
      |[ext_resource type="PackedScene" uid="uid://g76r1u8cf6n7" path="res://Player.tscn" id="3_s3hlu"]
      |[ext_resource type="PackedScene" uid="uid://8guvv3ewr5vx" path="res://Hud.tscn" id="4_10xq1"]
      |[ext_resource type="AudioStream" uid="uid://vnabgy5q8g1u" path="res://art/House In a Forest Loop.ogg" id="5_tpwrv"]
      |[ext_resource type="AudioStream" uid="uid://bwgfexqh0qy0c" path="res://art/gameover.wav" id="6_1uk8d"]
      |
      |[sub_resource type="Curve2D" id="Curve2D_6d42f"]
      |_data = {
      |"points": PackedVector2Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 480, 0, 0, 0, 0, 0, 480, 719, 0, 0, 0, 0, 0, 720, 0, 0, 0, 0, 0, 0)
      |}
      |point_count = 5
      |
      |[node name="Main" type="Node"]
      |script = ObjectValue.ExtResource("1_4hmc7")
      |MobScene = ObjectValue.ExtResource("2_q46v7")
      |
      |[node name="ColorRect" type="ColorRect" parent="."]
      |anchors_preset = 15
      |anchor_right = 1.0
      |anchor_bottom = 1.0
      |grow_horizontal = 2
      |grow_vertical = 2
      |color = Color(0, 0.25098, 0.25098, 1)
      |
      |[node name="Player" parent="." instance=ObjectValue.ExtResource("3_s3hlu")]
      |
      |[node name="MobTimer" type="Timer" parent="."]
      |wait_time = 0.5
      |
      |[node name="ScoreTimer" type="Timer" parent="."]
      |
      |[node name="StartTimer" type="Timer" parent="."]
      |wait_time = 2.0
      |one_shot = true
      |
      |[node name="StartPosition" type="Marker2D" parent="."]
      |position = Vector2(240, 450)
      |
      |[node name="MobPath" type="Path2D" parent="."]
      |curve = SubResource("Curve2D_6d42f")
      |
      |[node name="MobSpawnLocation" type="PathFollow2D" parent="MobPath"]
      |
      |[node name="Hud" parent="." instance=ObjectValue.ExtResource("4_10xq1")]
      |
      |[node name="Music" type="AudioStreamPlayer2D" parent="."]
      |stream = ObjectValue.ExtResource("5_tpwrv")
      |
      |[node name="DeathSound" type="AudioStreamPlayer2D" parent="."]
      |stream = ObjectValue.ExtResource("6_1uk8d")
      |
      |[connection signal="Hit" from="Player" to="." method="GameOver"]
      |[connection signal="timeout" from="MobTimer" to="." method="OnMobTimerTimeout"]
      |[connection signal="timeout" from="ScoreTimer" to="." method="OnScoreTimerTimeout"]
      |[connection signal="timeout" from="StartTimer" to="." method="OnStartTimerTimeout"]
      |[connection signal="StartGame" from="Hud" to="." method="NewGame"]
      |""".stripMargin

  private val tscnFourth =
    """
      |[gd_scene load_steps=4 format=3 uid="uid://c3sfqrrm1v350"]
      |
      |[ext_resource type="Texture2D" uid="uid://b35bon1qhoeuh" path="res://Assets/PathDisplay.png" id="1_rkbfj"]
      |[ext_resource type="Script" path="res://Scripts/PathTracker.cs" id="2_hwkby"]
      |
      |[sub_resource type="SpriteFrames" id="SpriteFrames_kwub8"]
      |animations = [{
      |"frames": [{
      |"duration": 1.0,
      |"texture": ObjectValue.ExtResource("1_rkbfj")
      |}],
      |"loop": true,
      |"name": &"default",
      |"speed": 5.0
      |}]
      |
      |[node name="Root" type="PathFollow2D"]
      |
      |[node name="Sprite" type="AnimatedSprite2D" parent="."]
      |sprite_frames = SubResource("SpriteFrames_kwub8")
      |script = ObjectValue.ExtResource("2_hwkby")
      |""".stripMargin

  private val tscnAdvanced =
    """
      |[gd_scene load_steps=32 format=3 uid="uid://4ujxbrmndad2"]
      |
      |[ext_resource type="PackedScene" uid="uid://bshd6dwdnxoca" path="res://Scenes/Adventure/AllyArea.tscn" id="1_htj2o"]
      |[ext_resource type="StyleBox" uid="uid://dhbndr1exet8b" path="res://Styles/GeneralPanel.tres" id="2_o6236"]
      |[ext_resource type="LabelSettings" uid="uid://bdlshe8bsoefc" path="res://Styles/DarkFont.tres" id="3_6jddl"]
      |[ext_resource type="Texture2D" uid="uid://ckg265j5wi4bd" path="res://Textures/HpFrame.png" id="4_2lal6"]
      |[ext_resource type="Texture2D" uid="uid://dew5mh2d0rrjr" path="res://Textures/HpFill.png" id="5_6tnpg"]
      |[ext_resource type="Texture2D" uid="uid://bbefokxamcmh2" path="res://Textures/Background/Dungeon1.png" id="7_k8514"]
      |[ext_resource type="StyleBox" uid="uid://hdey5ddoms6o" path="res://Styles/ThemedPanel.tres" id="7_qberl"]
      |[ext_resource type="PackedScene" uid="uid://d4ni6uubu6pl6" path="res://Scenes/Town/RoleCommand.tscn" id="7_usk6j"]
      |[ext_resource type="FontFile" uid="uid://c056irjwb6kk4" path="res://Resources/Styles/mplus-2p-regular.ttf" id="9_5k2lr"]
      |[ext_resource type="PackedScene" uid="uid://by34gbkawuexp" path="res://Scenes/Adventure/AdventureMenuButton.tscn" id="9_5qdy0"]
      |[ext_resource type="LabelSettings" uid="uid://bnpddypllwjny" path="res://Styles/RegularFont.tres" id="11_prhjf"]
      |
      |[sub_resource type="StyleBoxEmpty" id="StyleBoxEmpty_wg3gt"]
      |
      |[sub_resource type="StyleBoxEmpty" id="StyleBoxEmpty_qsdvr"]
      |
      |[sub_resource type="StyleBoxFlat" id="StyleBoxFlat_0cfoa"]
      |bg_color = Color(0.839216, 0.960784, 0.682353, 1)
      |
      |[sub_resource type="StyleBoxEmpty" id="StyleBoxEmpty_yv6d0"]
      |
      |[sub_resource type="StyleBoxEmpty" id="StyleBoxEmpty_i3ua8"]
      |
      |[sub_resource type="StyleBoxFlat" id="StyleBoxFlat_5u7r7"]
      |bg_color = Color(0.252286, 0.310152, 0.210365, 1)
      |border_color = Color(0.839216, 0.960784, 0.682353, 1)
      |
      |[sub_resource type="Theme" id="Theme_ial6e"]
      |resource_local_to_scene = true
      |Button/styles/disabled = SubResource("StyleBoxEmpty_wg3gt")
      |Button/styles/focus = SubResource("StyleBoxEmpty_qsdvr")
      |Button/styles/hover = SubResource("StyleBoxFlat_0cfoa")
      |Button/styles/normal = SubResource("StyleBoxEmpty_yv6d0")
      |Button/styles/pressed = SubResource("StyleBoxEmpty_i3ua8")
      |HoverableControl/styles/panel = null
      |Label/colors/font_color = Color(0.839216, 0.960784, 0.682353, 1)
      |Label/constants/line_spacing = 3
      |Label/font_sizes/font_size = 24
      |Label/fonts/font = ObjectValue.ExtResource("9_5k2lr")
      |PanelContainer/styles/panel = SubResource("StyleBoxFlat_5u7r7")
      |
      |[sub_resource type="StyleBoxEmpty" id="StyleBoxEmpty_wuuu5"]
      |
      |[sub_resource type="StyleBoxEmpty" id="StyleBoxEmpty_bog2v"]
      |
      |[sub_resource type="StyleBoxFlat" id="StyleBoxFlat_dh2c4"]
      |bg_color = Color(0.839216, 0.960784, 0.682353, 1)
      |
      |[sub_resource type="StyleBoxEmpty" id="StyleBoxEmpty_m6ehi"]
      |
      |[sub_resource type="StyleBoxEmpty" id="StyleBoxEmpty_cpir8"]
      |
      |[sub_resource type="StyleBoxFlat" id="StyleBoxFlat_h4s01"]
      |bg_color = Color(0.839216, 0.960784, 0.682353, 1)
      |
      |[sub_resource type="Theme" id="Theme_jd8wo"]
      |resource_local_to_scene = true
      |Button/colors/font_color = Color(0.252286, 0.310152, 0.210365, 1)
      |Button/font_sizes/font_size = 24
      |Button/fonts/font = ObjectValue.ExtResource("9_5k2lr")
      |Button/styles/disabled = SubResource("StyleBoxEmpty_wuuu5")
      |Button/styles/focus = SubResource("StyleBoxEmpty_bog2v")
      |Button/styles/hover = SubResource("StyleBoxFlat_dh2c4")
      |Button/styles/normal = SubResource("StyleBoxEmpty_m6ehi")
      |Button/styles/pressed = SubResource("StyleBoxEmpty_cpir8")
      |Label/colors/font_color = Color(0.252286, 0.310152, 0.210365, 1)
      |Label/constants/line_spacing = 3
      |Label/font_sizes/font_size = 24
      |Label/fonts/font = ObjectValue.ExtResource("9_5k2lr")
      |PanelContainer/styles/panel = SubResource("StyleBoxFlat_h4s01")
      |
      |[sub_resource type="Theme" id="Theme_w6bn7"]
      |resource_local_to_scene = true
      |Button/styles/disabled = SubResource("StyleBoxEmpty_wg3gt")
      |Button/styles/focus = SubResource("StyleBoxEmpty_qsdvr")
      |Button/styles/hover = SubResource("StyleBoxFlat_0cfoa")
      |Button/styles/normal = SubResource("StyleBoxEmpty_yv6d0")
      |Button/styles/pressed = SubResource("StyleBoxEmpty_i3ua8")
      |HoverableControl/styles/panel = null
      |Label/colors/font_color = Color(0.839216, 0.960784, 0.682353, 1)
      |Label/constants/line_spacing = 3
      |Label/font_sizes/font_size = 24
      |Label/fonts/font = ObjectValue.ExtResource("9_5k2lr")
      |PanelContainer/styles/panel = SubResource("StyleBoxFlat_5u7r7")
      |
      |[sub_resource type="Theme" id="Theme_cpxna"]
      |resource_local_to_scene = true
      |Button/colors/font_color = Color(0.252286, 0.310152, 0.210365, 1)
      |Button/font_sizes/font_size = 24
      |Button/fonts/font = ObjectValue.ExtResource("9_5k2lr")
      |Button/styles/disabled = SubResource("StyleBoxEmpty_wuuu5")
      |Button/styles/focus = SubResource("StyleBoxEmpty_bog2v")
      |Button/styles/hover = SubResource("StyleBoxFlat_dh2c4")
      |Button/styles/normal = SubResource("StyleBoxEmpty_m6ehi")
      |Button/styles/pressed = SubResource("StyleBoxEmpty_cpir8")
      |Label/colors/font_color = Color(0.252286, 0.310152, 0.210365, 1)
      |Label/constants/line_spacing = 3
      |Label/font_sizes/font_size = 24
      |Label/fonts/font = ObjectValue.ExtResource("9_5k2lr")
      |PanelContainer/styles/panel = SubResource("StyleBoxFlat_h4s01")
      |
      |[sub_resource type="Theme" id="Theme_2xomn"]
      |resource_local_to_scene = true
      |Button/styles/disabled = SubResource("StyleBoxEmpty_wg3gt")
      |Button/styles/focus = SubResource("StyleBoxEmpty_qsdvr")
      |Button/styles/hover = SubResource("StyleBoxFlat_0cfoa")
      |Button/styles/normal = SubResource("StyleBoxEmpty_yv6d0")
      |Button/styles/pressed = SubResource("StyleBoxEmpty_i3ua8")
      |HoverableControl/styles/panel = null
      |Label/colors/font_color = Color(0.839216, 0.960784, 0.682353, 1)
      |Label/constants/line_spacing = 3
      |Label/font_sizes/font_size = 24
      |Label/fonts/font = ObjectValue.ExtResource("9_5k2lr")
      |PanelContainer/styles/panel = SubResource("StyleBoxFlat_5u7r7")
      |
      |[sub_resource type="Theme" id="Theme_xcjvy"]
      |resource_local_to_scene = true
      |Button/colors/font_color = Color(0.252286, 0.310152, 0.210365, 1)
      |Button/font_sizes/font_size = 24
      |Button/fonts/font = ObjectValue.ExtResource("9_5k2lr")
      |Button/styles/disabled = SubResource("StyleBoxEmpty_wuuu5")
      |Button/styles/focus = SubResource("StyleBoxEmpty_bog2v")
      |Button/styles/hover = SubResource("StyleBoxFlat_dh2c4")
      |Button/styles/normal = SubResource("StyleBoxEmpty_m6ehi")
      |Button/styles/pressed = SubResource("StyleBoxEmpty_cpir8")
      |Label/colors/font_color = Color(0.252286, 0.310152, 0.210365, 1)
      |Label/constants/line_spacing = 3
      |Label/font_sizes/font_size = 24
      |Label/fonts/font = ObjectValue.ExtResource("9_5k2lr")
      |PanelContainer/styles/panel = SubResource("StyleBoxFlat_h4s01")
      |
      |[sub_resource type="StyleBoxEmpty" id="StyleBoxEmpty_w4yhy"]
      |
      |[sub_resource type="StyleBoxFlat" id="StyleBoxFlat_yb3xh"]
      |bg_color = Color(0.25098, 0.309804, 0.211765, 0.392157)
      |
      |[node name="Root" type="Node2D"]
      |
      |[node name="UiRoot" type="Control" parent="."]
      |custom_minimum_size = Vector2(1280, 720)
      |layout_mode = 3
      |anchors_preset = 15
      |anchor_right = 1.0
      |anchor_bottom = 1.0
      |offset_right = 1280.0
      |offset_bottom = 720.0
      |grow_horizontal = 2
      |grow_vertical = 2
      |mouse_filter = 2
      |
      |[node name="CampOverlayParent" type="Node" parent="UiRoot"]
      |unique_name_in_owner = true
      |
      |[node name="AllyArea" parent="UiRoot" instance=ObjectValue.ExtResource("1_htj2o")]
      |unique_name_in_owner = true
      |z_index = 12
      |layout_mode = 1
      |anchors_preset = 12
      |anchor_top = 1.0
      |anchor_right = 1.0
      |anchor_bottom = 1.0
      |offset_left = 0.0
      |offset_top = -164.0
      |offset_right = 0.0
      |offset_bottom = 0.0
      |grow_horizontal = 2
      |grow_vertical = 0
      |
      |[node name="ResearchWindow" type="PanelContainer" parent="UiRoot"]
      |unique_name_in_owner = true
      |layout_mode = 1
      |anchors_preset = 8
      |anchor_left = 0.5
      |anchor_top = 0.5
      |anchor_right = 0.5
      |anchor_bottom = 0.5
      |offset_left = -20.0
      |offset_top = -20.0
      |offset_right = 20.0
      |offset_bottom = 20.0
      |grow_horizontal = 2
      |grow_vertical = 2
      |theme_override_styles/panel = ObjectValue.ExtResource("2_o6236")
      |
      |[node name="MarginContainer" type="MarginContainer" parent="UiRoot/ResearchWindow"]
      |layout_mode = 2
      |theme_override_constants/margin_left = 10
      |theme_override_constants/margin_top = 10
      |theme_override_constants/margin_right = 10
      |theme_override_constants/margin_bottom = 10
      |
      |[node name="VBoxContainer" type="VBoxContainer" parent="UiRoot/ResearchWindow/MarginContainer"]
      |layout_mode = 2
      |
      |[node name="HBoxContainer" type="HBoxContainer" parent="UiRoot/ResearchWindow/MarginContainer/VBoxContainer"]
      |layout_mode = 2
      |theme_override_constants/separation = 20
      |
      |[node name="Title" type="Label" parent="UiRoot/ResearchWindow/MarginContainer/VBoxContainer/HBoxContainer"]
      |layout_mode = 2
      |text = "迷宮を探索中……"
      |label_settings = ObjectValue.ExtResource("3_6jddl")
      |
      |[node name="Percent" type="Label" parent="UiRoot/ResearchWindow/MarginContainer/VBoxContainer/HBoxContainer"]
      |unique_name_in_owner = true
      |layout_mode = 2
      |text = "50%"
      |label_settings = ObjectValue.ExtResource("3_6jddl")
      |
      |[node name="CenterContainer" type="CenterContainer" parent="UiRoot/ResearchWindow/MarginContainer/VBoxContainer"]
      |layout_mode = 2
      |
      |[node name="ResearchBar" type="TextureProgressBar" parent="UiRoot/ResearchWindow/MarginContainer/VBoxContainer/CenterContainer"]
      |unique_name_in_owner = true
      |layout_mode = 2
      |max_value = 1.0
      |step = 0.01
      |texture_under = ObjectValue.ExtResource("4_2lal6")
      |texture_progress = ObjectValue.ExtResource("5_6tnpg")
      |
      |[node name="BattleOverlayParent" type="Node" parent="UiRoot"]
      |unique_name_in_owner = true
      |
      |[node name="Root" parent="UiRoot/BattleOverlayParent" instance_placeholder="res://Scenes/Battle/BattleOverlay.tscn"]
      |
      |[node name="Menu" type="VBoxContainer" parent="UiRoot"]
      |unique_name_in_owner = true
      |visible = false
      |custom_minimum_size = Vector2(250, 0)
      |layout_mode = 1
      |anchors_preset = 4
      |anchor_top = 0.5
      |anchor_bottom = 0.5
      |offset_top = -20.0
      |offset_right = 59.0
      |offset_bottom = 20.0
      |grow_vertical = 2
      |
      |[node name="Member" parent="UiRoot/Menu" instance=ObjectValue.ExtResource("9_5qdy0")]
      |unique_name_in_owner = true
      |layout_mode = 2
      |text = "Stats"
      |
      |[node name="Items" parent="UiRoot/Menu" instance=ObjectValue.ExtResource("9_5qdy0")]
      |unique_name_in_owner = true
      |layout_mode = 2
      |text = "Items"
      |
      |[node name="Break" parent="UiRoot/Menu" instance=ObjectValue.ExtResource("9_5qdy0")]
      |unique_name_in_owner = true
      |layout_mode = 2
      |text = "Break"
      |
      |[node name="Menu2" type="PanelContainer" parent="UiRoot"]
      |unique_name_in_owner = true
      |layout_mode = 0
      |offset_left = -5.0
      |offset_top = 250.0
      |offset_right = 125.0
      |offset_bottom = 393.0
      |theme_override_styles/panel = ObjectValue.ExtResource("7_qberl")
      |
      |[node name="MarginContainer" type="MarginContainer" parent="UiRoot/Menu2"]
      |layout_mode = 2
      |theme_override_constants/margin_left = 20
      |theme_override_constants/margin_top = 10
      |theme_override_constants/margin_right = 10
      |theme_override_constants/margin_bottom = 10
      |
      |[node name="VBoxContainer" type="VBoxContainer" parent="UiRoot/Menu2/MarginContainer"]
      |layout_mode = 2
      |
      |[node name="MemberItem" parent="UiRoot/Menu2/MarginContainer/VBoxContainer" instance=ObjectValue.ExtResource("7_usk6j")]
      |unique_name_in_owner = true
      |layout_mode = 2
      |theme = SubResource("Theme_ial6e")
      |HoverTheme = SubResource("Theme_jd8wo")
      |
      |[node name="Label" parent="UiRoot/Menu2/MarginContainer/VBoxContainer/MemberItem/PanelContainer" index="0"]
      |text = "メンバー"
      |
      |[node name="ItemsItem" parent="UiRoot/Menu2/MarginContainer/VBoxContainer" instance=ObjectValue.ExtResource("7_usk6j")]
      |unique_name_in_owner = true
      |layout_mode = 2
      |theme = SubResource("Theme_w6bn7")
      |HoverTheme = SubResource("Theme_cpxna")
      |
      |[node name="Label" parent="UiRoot/Menu2/MarginContainer/VBoxContainer/ItemsItem/PanelContainer" index="0"]
      |text = "キャンプ"
      |
      |[node name="BreakItem" parent="UiRoot/Menu2/MarginContainer/VBoxContainer" instance=ObjectValue.ExtResource("7_usk6j")]
      |unique_name_in_owner = true
      |layout_mode = 2
      |theme = SubResource("Theme_2xomn")
      |HoverTheme = SubResource("Theme_xcjvy")
      |
      |[node name="Label" parent="UiRoot/Menu2/MarginContainer/VBoxContainer/BreakItem/PanelContainer" index="0"]
      |text = "脱出する"
      |
      |[node name="Sprite2D" type="Sprite2D" parent="."]
      |z_index = -10
      |texture = ObjectValue.ExtResource("7_k8514")
      |centered = false
      |
      |[node name="TalkOverlay" type="PanelContainer" parent="."]
      |z_index = 24
      |anchors_preset = 15
      |anchor_right = 1.0
      |anchor_bottom = 1.0
      |offset_right = 1280.0
      |offset_bottom = 720.0
      |grow_horizontal = 2
      |grow_vertical = 2
      |mouse_filter = 2
      |theme_override_styles/panel = SubResource("StyleBoxEmpty_w4yhy")
      |
      |[node name="TalkToHide" type="PanelContainer" parent="TalkOverlay"]
      |unique_name_in_owner = true
      |clip_contents = true
      |layout_mode = 2
      |size_flags_vertical = 8
      |theme_override_styles/panel = SubResource("StyleBoxFlat_yb3xh")
      |
      |[node name="MarginContainer" type="MarginContainer" parent="TalkOverlay/TalkToHide"]
      |clip_contents = true
      |layout_mode = 2
      |theme_override_constants/margin_top = 10
      |theme_override_constants/margin_bottom = 10
      |
      |[node name="VBoxContainer" type="VBoxContainer" parent="TalkOverlay/TalkToHide/MarginContainer"]
      |layout_mode = 2
      |size_flags_horizontal = 4
      |
      |[node name="NameTagArea" type="MarginContainer" parent="TalkOverlay/TalkToHide/MarginContainer/VBoxContainer"]
      |layout_mode = 2
      |theme_override_constants/margin_bottom = -10
      |
      |[node name="PanelContainer" type="PanelContainer" parent="TalkOverlay/TalkToHide/MarginContainer/VBoxContainer/NameTagArea"]
      |layout_mode = 2
      |size_flags_horizontal = 0
      |theme_override_styles/panel = ObjectValue.ExtResource("7_qberl")
      |
      |[node name="MarginContainer" type="MarginContainer" parent="TalkOverlay/TalkToHide/MarginContainer/VBoxContainer/NameTagArea/PanelContainer"]
      |layout_mode = 2
      |theme_override_constants/margin_left = 10
      |theme_override_constants/margin_right = 10
      |
      |[node name="TalkerName" type="Label" parent="TalkOverlay/TalkToHide/MarginContainer/VBoxContainer/NameTagArea/PanelContainer/MarginContainer"]
      |unique_name_in_owner = true
      |layout_mode = 2
      |text = "リリアン"
      |label_settings = ObjectValue.ExtResource("11_prhjf")
      |
      |[node name="TalkPanel" type="PanelContainer" parent="TalkOverlay/TalkToHide/MarginContainer/VBoxContainer"]
      |clip_contents = true
      |custom_minimum_size = Vector2(600, 135)
      |layout_mode = 2
      |size_flags_horizontal = 4
      |size_flags_vertical = 4
      |theme_override_styles/panel = ObjectValue.ExtResource("7_qberl")
      |
      |[node name="MarginContainer" type="MarginContainer" parent="TalkOverlay/TalkToHide/MarginContainer/VBoxContainer/TalkPanel"]
      |clip_contents = true
      |layout_mode = 2
      |theme_override_constants/margin_left = 30
      |theme_override_constants/margin_top = 10
      |theme_override_constants/margin_right = 30
      |theme_override_constants/margin_bottom = 10
      |
      |[node name="TalkBody" type="Label" parent="TalkOverlay/TalkToHide/MarginContainer/VBoxContainer/TalkPanel/MarginContainer"]
      |unique_name_in_owner = true
      |clip_contents = true
      |layout_mode = 2
      |size_flags_vertical = 0
      |text = "それ、チョコレート？
      |「シュガー・ウォーズ」のプロモグッズと
      |パッケージが似てるかも。"
      |label_settings = ObjectValue.ExtResource("11_prhjf")
      |vertical_alignment = 1
      |
      |[editable path="UiRoot/Menu/Member"]
      |[editable path="UiRoot/Menu2/MarginContainer/VBoxContainer/MemberItem"]
      |[editable path="UiRoot/Menu2/MarginContainer/VBoxContainer/ItemsItem"]
      |[editable path="UiRoot/Menu2/MarginContainer/VBoxContainer/BreakItem"]
      |""".stripMargin

  val first: PackedScene = PackedScene(
    loadSteps = 8,
    format = 3,
    uid = Some("uid://d3doqyggcpkeb"),
    extResources = Vector(
      ExtResource("1_8162q", "Script", "res://Player.cs", None),
      ExtResource("1_d8csi", "Texture2D", "res://art/playerGrey_up1.png", Some("uid://bxacee62lu81")),
      ExtResource("2_ljnug", "Texture2D", "res://art/playerGrey_up2.png", Some("uid://b70twminywsyj")),
      ExtResource("3_krmrv", "Texture2D", "res://art/playerGrey_walk1.png", Some("uid://81wtq6p1bwfg")),
      ExtResource("4_jrmwk", "Texture2D", "res://art/playerGrey_walk2.png", Some("uid://da45skrrq48dj"))
    ),
    subResources = Vector(
      SubResource(
        "SpriteFrames_707dc",
        "SpriteFrames",
        Map(
          "animations" -> Variant.Array(
            Vector(
              Variant.Dictionary(
                Map(
                  "frames" -> Variant.Array(
                    Vector(
                      Variant.Dictionary(
                        Map(
                          "duration" -> Variant.Float(1.0),
                          "texture" -> Variant.Object(ObjectValue.ExtResource("1_d8csi"))
                        ),
                        None
                      ),
                      Variant.Dictionary(
                        Map("duration" -> Variant.Float(1.0), "texture" -> Variant.Object(ObjectValue.ExtResource("2_ljnug"))),
                        None
                      )
                    ),
                    None
                  ),
                  "loop" -> Variant.Bool(true),
                  "name" -> Variant.StringName("up"),
                  "speed" -> Variant.Float(5.0)
                ),
                None
              ),
              Variant.Dictionary(
                Map(
                  "frames" -> Variant.Array(
                    Vector(
                      Variant.Dictionary(
                        Map("duration" -> Variant.Float(1.0), "texture" -> Variant.Object(ObjectValue.ExtResource("3_krmrv"))),
                        None
                      ),
                      Variant.Dictionary(
                        Map("duration" -> Variant.Float(1.0), "texture" -> Variant.Object(ObjectValue.ExtResource("4_jrmwk"))),
                        None
                      )
                    ),
                    None
                  ),
                  "loop" -> Variant.Bool(true),
                  "name" -> Variant.StringName("walk"),
                  "speed" -> Variant.Float(5.0)
                ),
                None
              )
            ),
            None
          )
        )
      ),
      SubResource(
        "CapsuleShape2D_6tr4r",
        "CapsuleShape2D",
        Map("radius" -> Variant.Float(27.0), "height" -> Variant.Float(70.0))
      )
    ),
    nodes = Vector(
      NodeData(
        "Player",
        Some("Area2D"),
        None,
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map("script" -> Variant.Object(ObjectValue.ExtResource("1_8162q")), "metadata/_edit_group_" -> Variant.Bool(true))
      ),
      NodeData(
        "AnimatedSprite2d",
        Some("AnimatedSprite2D"),
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "scale" -> Variant.Vector2(0.5, 0.5),
          "sprite_frames" -> Variant.Object(ObjectValue.SubResource("SpriteFrames_707dc")),
          "animation" -> Variant.StringName("up")
        )
      ),
      NodeData(
        "CollisionShape2d",
        Some("CollisionShape2D"),
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map("shape" -> Variant.Object(ObjectValue.SubResource("CapsuleShape2D_6tr4r")))
      )
    ),
    connections = Vector(
      ConnectionData("body_entered", ".", Vector.empty, ".", Vector.empty, "_on_body_entered", 0, Vector.empty, 0)
    ),
    editableInstances = Vector.empty
  )
  val second: PackedScene = PackedScene(
    loadSteps = 10,
    format = 3,
    uid = Some("uid://buj8wisq0l07p"),
    extResources = Vector(
      ExtResource("1_k5y70", "Script", "res://Mob.cs", None),
      ExtResource("1_ytrj5", "Texture2D", "res://art/enemyFlyingAlt_1.png", Some("uid://bye51w2ru5vie")),
      ExtResource("2_bls2m", "Texture2D", "res://art/enemyFlyingAlt_2.png", Some("uid://dhfn0d6qh0qvy")),
      ExtResource("3_j0cqp", "Texture2D", "res://art/enemySwimming_1.png", Some("uid://bwrqjt5jq5xfb")),
      ExtResource("4_bnymk", "Texture2D", "res://art/enemySwimming_2.png", Some("uid://c5uy3836dmlt5")),
      ExtResource("5_kl1oy", "Texture2D", "res://art/enemyWalking_1.png", Some("uid://cdv0chn06a4bm")),
      ExtResource("6_h88k2", "Texture2D", "res://art/enemyWalking_2.png", Some("uid://cr0p08wnu2xm8"))
    ),
    subResources = Vector(
      SubResource(
        "SpriteFrames_7jrot",
        "SpriteFrames",
        Map(
          "animations" -> Variant.Array(
            Vector(
              Variant.Dictionary(
                Map(
                  "frames" -> Variant.Array(
                    Vector(
                      Variant.Dictionary(
                        Map("duration" -> Variant.Float(1.0), "texture" -> Variant.Object(ObjectValue.ExtResource("1_ytrj5"))),
                        None
                      ),
                      Variant.Dictionary(
                        Map("duration" -> Variant.Float(1.0), "texture" -> Variant.Object(ObjectValue.ExtResource("2_bls2m"))),
                        None
                      )
                    ),
                    None
                  ),
                  "loop" -> Variant.Bool(true),
                  "name" -> Variant.StringName("fly"),
                  "speed" -> Variant.Float(3.0)
                ),
                None
              ),
              Variant.Dictionary(
                Map(
                  "frames" -> Variant.Array(
                    Vector(
                      Variant.Dictionary(
                        Map("duration" -> Variant.Float(1.0), "texture" -> Variant.Object(ObjectValue.ExtResource("3_j0cqp"))),
                        None
                      ),
                      Variant.Dictionary(
                        Map("duration" -> Variant.Float(1.0), "texture" -> Variant.Object(ObjectValue.ExtResource("4_bnymk"))),
                        None
                      )
                    ),
                    None
                  ),
                  "loop" -> Variant.Bool(true),
                  "name" -> Variant.StringName("swim"),
                  "speed" -> Variant.Float(3.0)
                ),
                None
              ),
              Variant.Dictionary(
                Map(
                  "frames" -> Variant.Array(
                    Vector(
                      Variant.Dictionary(
                        Map("duration" -> Variant.Float(1.0), "texture" -> Variant.Object(ObjectValue.ExtResource("5_kl1oy"))),
                        None
                      ),
                      Variant.Dictionary(
                        Map("duration" -> Variant.Float(1.0), "texture" -> Variant.Object(ObjectValue.ExtResource("6_h88k2"))),
                        None
                      )
                    ),
                    None
                  ),
                  "loop" -> Variant.Bool(true),
                  "name" -> Variant.StringName("walk"),
                  "speed" -> Variant.Float(3.0)
                ),
                None
              )
            ),
            None
          )
        )
      ),
      SubResource(
        "CapsuleShape2D_r7hl5",
        "CapsuleShape2D",
        Map("radius" -> Variant.Float(58.0), "height" -> Variant.Float(120.0))
      )
    ),
    nodes = Vector(
      NodeData(
        "Mob",
        Some("RigidBody2D"),
        None,
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "collision_mask" -> Variant.Int(0L),
          "gravity_scale" -> Variant.Float(0.0),
          "script" -> Variant.Object(ObjectValue.ExtResource("1_k5y70")),
          "metadata/_edit_group_" -> Variant.Bool(true)
        )
      ),
      NodeData(
        "AnimatedSprite2D",
        Some("AnimatedSprite2D"),
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "scale" -> Variant.Vector2(0.75, 0.75),
          "sprite_frames" -> Variant.Object(ObjectValue.SubResource("SpriteFrames_7jrot")),
          "animation" -> Variant.StringName("swim")
        )
      ),
      NodeData(
        "VisibleOnScreenNotifier2D",
        Some("VisibleOnScreenNotifier2D"),
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map.empty
      ),
      NodeData(
        "CollisionShape2D",
        Some("CollisionShape2D"),
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "rotation" -> Variant.Float(1.5708),
          "shape" -> Variant.Object(ObjectValue.SubResource("CapsuleShape2D_r7hl5"))
        )
      )
    ),
    connections = Vector(
      ConnectionData(
        "screen_exited",
        "VisibleOnScreenNotifier2D",
        Vector.empty,
        ".",
        Vector.empty,
        "OnVisibleOnScreenNotifier2DScreenExited",
        0,
        Vector.empty,
        0
      )
    ),
    editableInstances = Vector.empty
  )
  val third: PackedScene = PackedScene(
    loadSteps = 8,
    format = 3,
    uid = Some("uid://hc1rholre01q"),
    extResources = Vector(
      ExtResource("1_4hmc7", "Script", "res://Main.cs", None),
      ExtResource("2_q46v7", "PackedScene", "res://Mob.tscn", Some("uid://05vlkouevqb2")),
      ExtResource("3_s3hlu", "PackedScene", "res://Player.tscn", Some("uid://g76r1u8cf6n7")),
      ExtResource("4_10xq1", "PackedScene", "res://Hud.tscn", Some("uid://8guvv3ewr5vx")),
      ExtResource("5_tpwrv", "AudioStream", "res://art/House In a Forest Loop.ogg", Some("uid://vnabgy5q8g1u")),
      ExtResource("6_1uk8d", "AudioStream", "res://art/gameover.wav", Some("uid://bwgfexqh0qy0c"))
    ),
    subResources = Vector(
      SubResource(
        "Curve2D_6d42f",
        "Curve2D",
        Map(
          "_data" -> Variant.Dictionary(
            Map(
              "points" -> Variant.PackedVector2Array(Vector(
                (0.0, 0.0),
                (0.0, 0.0),
                (0.0, 0.0),
                (0.0, 0.0),
                (0.0, 0.0),
                (480.0, 0.0),
                (0.0, 0.0),
                (0.0, 0.0),
                (480.0, 719.0),
                (0.0, 0.0),
                (0.0, 0.0),
                (0.0, 720.0),
                (0.0, 0.0),
                (0.0, 0.0),
                (0.0, 0.0)
              ))
            ),
            None
          ),
          "point_count" -> Variant.Int(5L)
        )
      )
    ),
    nodes = Vector(
      NodeData(
        "Main",
        Some("Node"),
        None,
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "script" -> Variant.Object(ObjectValue.ExtResource("1_4hmc7")),
          "MobScene" -> Variant.Object(ObjectValue.ExtResource("2_q46v7"))
        )
      ),
      NodeData(
        "ColorRect",
        Some("ColorRect"),
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "anchors_preset" -> Variant.Int(15L),
          "anchor_right" -> Variant.Float(1.0),
          "anchor_bottom" -> Variant.Float(1.0),
          "grow_horizontal" -> Variant.Int(2L),
          "grow_vertical" -> Variant.Int(2L),
          "color" -> Variant.Color(0.0, 0.25098, 0.25098, 1.0)
        )
      ),
      NodeData(
        "Player",
        None,
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        Some("3_s3hlu"),
        None,
        Vector.empty,
        Vector.empty,
        Map.empty
      ),
      NodeData(
        "MobTimer",
        Some("Timer"),
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map("wait_time" -> Variant.Float(0.5))
      ),
      NodeData(
        "ScoreTimer",
        Some("Timer"),
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map.empty
      ),
      NodeData(
        "StartTimer",
        Some("Timer"),
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "wait_time" -> Variant.Float(2.0),
          "one_shot" -> Variant.Bool(true)
        )
      ),
      NodeData(
        "StartPosition",
        Some("Marker2D"),
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map("position" -> Variant.Vector2(240.0, 450.0))
      ),
      NodeData(
        "MobPath",
        Some("Path2D"),
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map("curve" -> Variant.Object(ObjectValue.SubResource("Curve2D_6d42f")))
      ),
      NodeData(
        "MobSpawnLocation",
        Some("PathFollow2D"),
        Some("MobPath"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map.empty
      ),
      NodeData(
        "Hud",
        None,
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        Some("4_10xq1"),
        None,
        Vector.empty,
        Vector.empty,
        Map.empty
      ),
      NodeData(
        "Music",
        Some("AudioStreamPlayer2D"),
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map("stream" -> Variant.Object(ObjectValue.ExtResource("5_tpwrv")))
      ),
      NodeData(
        "DeathSound",
        Some("AudioStreamPlayer2D"),
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map("stream" -> Variant.Object(ObjectValue.ExtResource("6_1uk8d")))
      )
    ),
    connections = Vector(
      ConnectionData("Hit", "Player", Vector.empty, ".", Vector.empty, "GameOver", 0, Vector.empty, 0),
      ConnectionData("timeout", "MobTimer", Vector.empty, ".", Vector.empty, "OnMobTimerTimeout", 0, Vector.empty, 0),
      ConnectionData(
        "timeout",
        "ScoreTimer",
        Vector.empty,
        ".",
        Vector.empty,
        "OnScoreTimerTimeout",
        0,
        Vector.empty,
        0
      ),
      ConnectionData(
        "timeout",
        "StartTimer",
        Vector.empty,
        ".",
        Vector.empty,
        "OnStartTimerTimeout",
        0,
        Vector.empty,
        0
      ),
      ConnectionData("StartGame", "Hud", Vector.empty, ".", Vector.empty, "NewGame", 0, Vector.empty, 0)
    ),
    editableInstances = Vector.empty
  )
  val fourth: PackedScene = PackedScene(
    loadSteps = 4,
    format = 3,
    uid = Some("uid://c3sfqrrm1v350"),
    extResources = Vector(
      ExtResource("1_rkbfj", "Texture2D", "res://Assets/PathDisplay.png", Some("uid://b35bon1qhoeuh")),
      ExtResource("2_hwkby", "Script", "res://Scripts/PathTracker.cs", None)
    ),
    subResources = Vector(
      SubResource(
        "SpriteFrames_kwub8",
        "SpriteFrames",
        Map(
          "animations" -> Variant.Array(
            Vector(
              Variant.Dictionary(
                Map(
                  "frames" -> Variant.Array(
                    Vector(
                      Variant.Dictionary(
                        Map("duration" -> Variant.Float(1.0), "texture" -> Variant.Object(ObjectValue.ExtResource("1_rkbfj"))),
                        None
                      )
                    ),
                    None
                  ),
                  "loop" -> Variant.Bool(true),
                  "name" -> Variant.StringName("default"),
                  "speed" -> Variant.Float(5.0)
                ),
                None
              )
            ),
            None
          )
        )
      )
    ),
    nodes = Vector(
      NodeData(
        "Root",
        Some("PathFollow2D"),
        None,
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map.empty
      ),
      NodeData(
        "Sprite",
        Some("AnimatedSprite2D"),
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "sprite_frames" -> Variant.Object(ObjectValue.SubResource("SpriteFrames_kwub8")),
          "script" -> Variant.Object(ObjectValue.ExtResource("2_hwkby"))
        )
      )
    ),
    connections = Vector.empty,
    editableInstances = Vector.empty
  )
  val advanced: PackedScene = PackedScene(
    loadSteps = 32,
    format = 3,
    uid = Some("uid://4ujxbrmndad2"),
    extResources = Vector(
      ExtResource("1_htj2o", "PackedScene", "res://Scenes/Adventure/AllyArea.tscn", Some("uid://bshd6dwdnxoca")),
      ExtResource("2_o6236", "StyleBox", "res://Styles/GeneralPanel.tres", Some("uid://dhbndr1exet8b")),
      ExtResource("3_6jddl", "LabelSettings", "res://Styles/DarkFont.tres", Some("uid://bdlshe8bsoefc")),
      ExtResource("4_2lal6", "Texture2D", "res://Textures/HpFrame.png", Some("uid://ckg265j5wi4bd")),
      ExtResource("5_6tnpg", "Texture2D", "res://Textures/HpFill.png", Some("uid://dew5mh2d0rrjr")),
      ExtResource("7_k8514", "Texture2D", "res://Textures/Background/Dungeon1.png", Some("uid://bbefokxamcmh2")),
      ExtResource("7_qberl", "StyleBox", "res://Styles/ThemedPanel.tres", Some("uid://hdey5ddoms6o")),
      ExtResource("7_usk6j", "PackedScene", "res://Scenes/Town/RoleCommand.tscn", Some("uid://d4ni6uubu6pl6")),
      ExtResource("9_5k2lr", "FontFile", "res://Resources/Styles/mplus-2p-regular.ttf", Some("uid://c056irjwb6kk4")),
      ExtResource(
        "9_5qdy0",
        "PackedScene",
        "res://Scenes/Adventure/AdventureMenuButton.tscn",
        Some("uid://by34gbkawuexp")
      ),
      ExtResource("11_prhjf", "LabelSettings", "res://Styles/RegularFont.tres", Some("uid://bnpddypllwjny"))
    ),
    subResources = Vector(
      SubResource("StyleBoxEmpty_wg3gt", "StyleBoxEmpty", Map.empty),
      SubResource("StyleBoxEmpty_qsdvr", "StyleBoxEmpty", Map.empty),
      SubResource(
        "StyleBoxFlat_0cfoa",
        "StyleBoxFlat",
        Map("bg_color" -> Variant.Color(0.839216, 0.960784, 0.682353, 1.0))
      ),
      SubResource("StyleBoxEmpty_yv6d0", "StyleBoxEmpty", Map.empty),
      SubResource("StyleBoxEmpty_i3ua8", "StyleBoxEmpty", Map.empty),
      SubResource(
        "StyleBoxFlat_5u7r7",
        "StyleBoxFlat",
        Map(
          "bg_color" -> Variant.Color(0.252286, 0.310152, 0.210365, 1.0),
          "border_color" -> Variant.Color(0.839216, 0.960784, 0.682353, 1.0)
        )
      ),
      SubResource(
        "Theme_ial6e",
        "Theme",
        Map(
          "resource_local_to_scene" -> Variant.Bool(true),
          "Button/styles/disabled" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_wg3gt")),
          "Button/styles/focus" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_qsdvr")),
          "Button/styles/hover" -> Variant.Object(ObjectValue.SubResource("StyleBoxFlat_0cfoa")),
          "Button/styles/normal" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_yv6d0")),
          "Button/styles/pressed" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_i3ua8")),
          "HoverableControl/styles/panel" -> Variant.Object(ObjectValue.Null),
          "Label/colors/font_color" -> Variant.Color(0.839216, 0.960784, 0.682353, 1.0),
          "Label/constants/line_spacing" -> Variant.Int(3L),
          "Label/font_sizes/font_size" -> Variant.Int(24L),
          "Label/fonts/font" -> Variant.Object(ObjectValue.ExtResource("9_5k2lr")),
          "PanelContainer/styles/panel" -> Variant.Object(ObjectValue.SubResource("StyleBoxFlat_5u7r7"))
        )
      ),
      SubResource("StyleBoxEmpty_wuuu5", "StyleBoxEmpty", Map.empty),
      SubResource("StyleBoxEmpty_bog2v", "StyleBoxEmpty", Map.empty),
      SubResource(
        "StyleBoxFlat_dh2c4",
        "StyleBoxFlat",
        Map("bg_color" -> Variant.Color(0.839216, 0.960784, 0.682353, 1.0))
      ),
      SubResource("StyleBoxEmpty_m6ehi", "StyleBoxEmpty", Map.empty),
      SubResource("StyleBoxEmpty_cpir8", "StyleBoxEmpty", Map.empty),
      SubResource(
        "StyleBoxFlat_h4s01",
        "StyleBoxFlat",
        Map("bg_color" -> Variant.Color(0.839216, 0.960784, 0.682353, 1.0))
      ),
      SubResource(
        "Theme_jd8wo",
        "Theme",
        Map(
          "resource_local_to_scene" -> Variant.Bool(true),
          "Button/colors/font_color" -> Variant.Color(0.252286, 0.310152, 0.210365, 1.0),
          "Button/font_sizes/font_size" -> Variant.Int(24L),
          "Button/fonts/font" -> Variant.Object(ObjectValue.ExtResource("9_5k2lr")),
          "Button/styles/disabled" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_wuuu5")),
          "Button/styles/focus" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_bog2v")),
          "Button/styles/hover" -> Variant.Object(ObjectValue.SubResource("StyleBoxFlat_dh2c4")),
          "Button/styles/normal" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_m6ehi")),
          "Button/styles/pressed" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_cpir8")),
          "Label/colors/font_color" -> Variant.Color(0.252286, 0.310152, 0.210365, 1.0),
          "Label/constants/line_spacing" -> Variant.Int(3L),
          "Label/font_sizes/font_size" -> Variant.Int(24L),
          "Label/fonts/font" -> Variant.Object(ObjectValue.ExtResource("9_5k2lr")),
          "PanelContainer/styles/panel" -> Variant.Object(ObjectValue.SubResource("StyleBoxFlat_h4s01"))
        )
      ),
      SubResource(
        "Theme_w6bn7",
        "Theme",
        Map(
          "resource_local_to_scene" -> Variant.Bool(true),
          "Button/styles/disabled" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_wg3gt")),
          "Button/styles/focus" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_qsdvr")),
          "Button/styles/hover" -> Variant.Object(ObjectValue.SubResource("StyleBoxFlat_0cfoa")),
          "Button/styles/normal" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_yv6d0")),
          "Button/styles/pressed" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_i3ua8")),
          "HoverableControl/styles/panel" -> Variant.Object(ObjectValue.Null),
          "Label/colors/font_color" -> Variant.Color(0.839216, 0.960784, 0.682353, 1.0),
          "Label/constants/line_spacing" -> Variant.Int(3L),
          "Label/font_sizes/font_size" -> Variant.Int(24L),
          "Label/fonts/font" -> Variant.Object(ObjectValue.ExtResource("9_5k2lr")),
          "PanelContainer/styles/panel" -> Variant.Object(ObjectValue.SubResource("StyleBoxFlat_5u7r7"))
        )
      ),
      SubResource(
        "Theme_cpxna",
        "Theme",
        Map(
          "resource_local_to_scene" -> Variant.Bool(true),
          "Button/colors/font_color" -> Variant.Color(0.252286, 0.310152, 0.210365, 1.0),
          "Button/font_sizes/font_size" -> Variant.Int(24L),
          "Button/fonts/font" -> Variant.Object(ObjectValue.ExtResource("9_5k2lr")),
          "Button/styles/disabled" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_wuuu5")),
          "Button/styles/focus" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_bog2v")),
          "Button/styles/hover" -> Variant.Object(ObjectValue.SubResource("StyleBoxFlat_dh2c4")),
          "Button/styles/normal" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_m6ehi")),
          "Button/styles/pressed" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_cpir8")),
          "Label/colors/font_color" -> Variant.Color(0.252286, 0.310152, 0.210365, 1.0),
          "Label/constants/line_spacing" -> Variant.Int(3L),
          "Label/font_sizes/font_size" -> Variant.Int(24L),
          "Label/fonts/font" -> Variant.Object(ObjectValue.ExtResource("9_5k2lr")),
          "PanelContainer/styles/panel" -> Variant.Object(ObjectValue.SubResource("StyleBoxFlat_h4s01"))
        )
      ),
      SubResource(
        "Theme_2xomn",
        "Theme",
        Map(
          "resource_local_to_scene" -> Variant.Bool(true),
          "Button/styles/disabled" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_wg3gt")),
          "Button/styles/focus" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_qsdvr")),
          "Button/styles/hover" -> Variant.Object(ObjectValue.SubResource("StyleBoxFlat_0cfoa")),
          "Button/styles/normal" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_yv6d0")),
          "Button/styles/pressed" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_i3ua8")),
          "HoverableControl/styles/panel" -> Variant.Object(ObjectValue.Null),
          "Label/colors/font_color" -> Variant.Color(0.839216, 0.960784, 0.682353, 1.0),
          "Label/constants/line_spacing" -> Variant.Int(3L),
          "Label/font_sizes/font_size" -> Variant.Int(24L),
          "Label/fonts/font" -> Variant.Object(ObjectValue.ExtResource("9_5k2lr")),
          "PanelContainer/styles/panel" -> Variant.Object(ObjectValue.SubResource("StyleBoxFlat_5u7r7"))
        )
      ),
      SubResource(
        "Theme_xcjvy",
        "Theme",
        Map(
          "resource_local_to_scene" -> Variant.Bool(true),
          "Button/colors/font_color" -> Variant.Color(0.252286, 0.310152, 0.210365, 1.0),
          "Button/font_sizes/font_size" -> Variant.Int(24L),
          "Button/fonts/font" -> Variant.Object(ObjectValue.ExtResource("9_5k2lr")),
          "Button/styles/disabled" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_wuuu5")),
          "Button/styles/focus" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_bog2v")),
          "Button/styles/hover" -> Variant.Object(ObjectValue.SubResource("StyleBoxFlat_dh2c4")),
          "Button/styles/normal" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_m6ehi")),
          "Button/styles/pressed" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_cpir8")),
          "Label/colors/font_color" -> Variant.Color(0.252286, 0.310152, 0.210365, 1.0),
          "Label/constants/line_spacing" -> Variant.Int(3L),
          "Label/font_sizes/font_size" -> Variant.Int(24L),
          "Label/fonts/font" -> Variant.Object(ObjectValue.ExtResource("9_5k2lr")),
          "PanelContainer/styles/panel" -> Variant.Object(ObjectValue.SubResource("StyleBoxFlat_h4s01"))
        )
      ),
      SubResource("StyleBoxEmpty_w4yhy", "StyleBoxEmpty", Map.empty),
      SubResource(
        "StyleBoxFlat_yb3xh",
        "StyleBoxFlat",
        Map("bg_color" -> Variant.Color(0.25098, 0.309804, 0.211765, 0.392157))
      )
    ),
    nodes = Vector(
      NodeData(
        "Root",
        Some("Node2D"),
        None,
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map.empty
      ),
      NodeData(
        "UiRoot",
        Some("Control"),
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "custom_minimum_size" -> Variant.Vector2(1280.0, 720.0),
          "layout_mode" -> Variant.Int(3L),
          "anchors_preset" -> Variant.Int(15L),
          "anchor_right" -> Variant.Float(1.0),
          "anchor_bottom" -> Variant.Float(1.0),
          "offset_right" -> Variant.Float(1280.0),
          "offset_bottom" -> Variant.Float(720.0),
          "grow_horizontal" -> Variant.Int(2L),
          "grow_vertical" -> Variant.Int(2L),
          "mouse_filter" -> Variant.Int(2L)
        )
      ),
      NodeData(
        "CampOverlayParent",
        Some("Node"),
        Some("UiRoot"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map("unique_name_in_owner" -> Variant.Bool(true))
      ),
      NodeData(
        "AllyArea",
        None,
        Some("UiRoot"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        Some("1_htj2o"),
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "unique_name_in_owner" -> Variant.Bool(true),
          "z_index" -> Variant.Int(12L),
          "layout_mode" -> Variant.Int(1L),
          "anchors_preset" -> Variant.Int(12L),
          "anchor_top" -> Variant.Float(1.0),
          "anchor_right" -> Variant.Float(1.0),
          "anchor_bottom" -> Variant.Float(1.0),
          "offset_left" -> Variant.Float(0.0),
          "offset_top" -> Variant.Float(-164.0),
          "offset_right" -> Variant.Float(0.0),
          "offset_bottom" -> Variant.Float(0.0),
          "grow_horizontal" -> Variant.Int(2L),
          "grow_vertical" -> Variant.Int(0L)
        )
      ),
      NodeData(
        "ResearchWindow",
        Some("PanelContainer"),
        Some("UiRoot"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "unique_name_in_owner" -> Variant.Bool(true),
          "layout_mode" -> Variant.Int(1L),
          "anchors_preset" -> Variant.Int(8L),
          "anchor_left" -> Variant.Float(0.5),
          "anchor_top" -> Variant.Float(0.5),
          "anchor_right" -> Variant.Float(0.5),
          "anchor_bottom" -> Variant.Float(0.5),
          "offset_left" -> Variant.Float(-20.0),
          "offset_top" -> Variant.Float(-20.0),
          "offset_right" -> Variant.Float(20.0),
          "offset_bottom" -> Variant.Float(20.0),
          "grow_horizontal" -> Variant.Int(2L),
          "grow_vertical" -> Variant.Int(2L),
          "theme_override_styles/panel" -> Variant.Object(ObjectValue.ExtResource("2_o6236"))
        )
      ),
      NodeData(
        "MarginContainer",
        Some("MarginContainer"),
        Some("UiRoot/ResearchWindow"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "layout_mode" -> Variant.Int(2L),
          "theme_override_constants/margin_left" -> Variant.Int(10L),
          "theme_override_constants/margin_top" -> Variant.Int(10L),
          "theme_override_constants/margin_right" -> Variant.Int(10L),
          "theme_override_constants/margin_bottom" -> Variant.Int(10L)
        )
      ),
      NodeData(
        "VBoxContainer",
        Some("VBoxContainer"),
        Some("UiRoot/ResearchWindow/MarginContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map("layout_mode" -> Variant.Int(2L))
      ),
      NodeData(
        "HBoxContainer",
        Some("HBoxContainer"),
        Some("UiRoot/ResearchWindow/MarginContainer/VBoxContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "layout_mode" -> Variant.Int(2L),
          "theme_override_constants/separation" -> Variant.Int(20L)
        )
      ),
      NodeData(
        "Title",
        Some("Label"),
        Some("UiRoot/ResearchWindow/MarginContainer/VBoxContainer/HBoxContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "layout_mode" -> Variant.Int(2L),
          "text" -> Variant.String("迷宮を探索中……"),
          "label_settings" -> Variant.Object(ObjectValue.ExtResource("3_6jddl"))
        )
      ),
      NodeData(
        "Percent",
        Some("Label"),
        Some("UiRoot/ResearchWindow/MarginContainer/VBoxContainer/HBoxContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "unique_name_in_owner" -> Variant.Bool(true),
          "layout_mode" -> Variant.Int(2L),
          "text" -> Variant.String("50%"),
          "label_settings" -> Variant.Object(ObjectValue.ExtResource("3_6jddl"))
        )
      ),
      NodeData(
        "CenterContainer",
        Some("CenterContainer"),
        Some("UiRoot/ResearchWindow/MarginContainer/VBoxContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map("layout_mode" -> Variant.Int(2L))
      ),
      NodeData(
        "ResearchBar",
        Some("TextureProgressBar"),
        Some("UiRoot/ResearchWindow/MarginContainer/VBoxContainer/CenterContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "unique_name_in_owner" -> Variant.Bool(true),
          "layout_mode" -> Variant.Int(2L),
          "max_value" -> Variant.Float(1.0),
          "step" -> Variant.Float(0.01),
          "texture_under" -> Variant.Object(ObjectValue.ExtResource("4_2lal6")),
          "texture_progress" -> Variant.Object(ObjectValue.ExtResource("5_6tnpg"))
        )
      ),
      NodeData(
        "BattleOverlayParent",
        Some("Node"),
        Some("UiRoot"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map("unique_name_in_owner" -> Variant.Bool(true))
      ),
      NodeData(
        "Root",
        None,
        Some("UiRoot/BattleOverlayParent"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        Some("res://Scenes/Battle/BattleOverlay.tscn"),
        Vector.empty,
        Vector.empty,
        Map.empty
      ),
      NodeData(
        "Menu",
        Some("VBoxContainer"),
        Some("UiRoot"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "unique_name_in_owner" -> Variant.Bool(true),
          "visible" -> Variant.Bool(false),
          "custom_minimum_size" -> Variant.Vector2(250.0, 0.0),
          "layout_mode" -> Variant.Int(1L),
          "anchors_preset" -> Variant.Int(4L),
          "anchor_top" -> Variant.Float(0.5),
          "anchor_bottom" -> Variant.Float(0.5),
          "offset_top" -> Variant.Float(-20.0),
          "offset_right" -> Variant.Float(59.0),
          "offset_bottom" -> Variant.Float(20.0),
          "grow_vertical" -> Variant.Int(2L)
        )
      ),
      NodeData(
        "Member",
        None,
        Some("UiRoot/Menu"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        Some("9_5qdy0"),
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "unique_name_in_owner" -> Variant.Bool(true),
          "layout_mode" -> Variant.Int(2L),
          "text" -> Variant.String("Stats")
        )
      ),
      NodeData(
        "Items",
        None,
        Some("UiRoot/Menu"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        Some("9_5qdy0"),
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "unique_name_in_owner" -> Variant.Bool(true),
          "layout_mode" -> Variant.Int(2L),
          "text" -> Variant.String("Items")
        )
      ),
      NodeData(
        "Break",
        None,
        Some("UiRoot/Menu"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        Some("9_5qdy0"),
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "unique_name_in_owner" -> Variant.Bool(true),
          "layout_mode" -> Variant.Int(2L),
          "text" -> Variant.String("Break")
        )
      ),
      NodeData(
        "Menu2",
        Some("PanelContainer"),
        Some("UiRoot"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "unique_name_in_owner" -> Variant.Bool(true),
          "layout_mode" -> Variant.Int(0L),
          "offset_left" -> Variant.Float(-5.0),
          "offset_top" -> Variant.Float(250.0),
          "offset_right" -> Variant.Float(125.0),
          "offset_bottom" -> Variant.Float(393.0),
          "theme_override_styles/panel" -> Variant.Object(ObjectValue.ExtResource("7_qberl"))
        )
      ),
      NodeData(
        "MarginContainer",
        Some("MarginContainer"),
        Some("UiRoot/Menu2"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "layout_mode" -> Variant.Int(2L),
          "theme_override_constants/margin_left" -> Variant.Int(20L),
          "theme_override_constants/margin_top" -> Variant.Int(10L),
          "theme_override_constants/margin_right" -> Variant.Int(10L),
          "theme_override_constants/margin_bottom" -> Variant.Int(10L)
        )
      ),
      NodeData(
        "VBoxContainer",
        Some("VBoxContainer"),
        Some("UiRoot/Menu2/MarginContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map("layout_mode" -> Variant.Int(2L))
      ),
      NodeData(
        "MemberItem",
        None,
        Some("UiRoot/Menu2/MarginContainer/VBoxContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        Some("7_usk6j"),
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "unique_name_in_owner" -> Variant.Bool(true),
          "layout_mode" -> Variant.Int(2L),
          "theme" -> Variant.Object(ObjectValue.SubResource("Theme_ial6e")),
          "HoverTheme" -> Variant.Object(ObjectValue.SubResource("Theme_jd8wo"))
        )
      ),
      NodeData(
        "Label",
        None,
        Some("UiRoot/Menu2/MarginContainer/VBoxContainer/MemberItem/PanelContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map("text" -> Variant.String("メンバー"))
      ),
      NodeData(
        "ItemsItem",
        None,
        Some("UiRoot/Menu2/MarginContainer/VBoxContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        Some("7_usk6j"),
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "unique_name_in_owner" -> Variant.Bool(true),
          "layout_mode" -> Variant.Int(2L),
          "theme" -> Variant.Object(ObjectValue.SubResource("Theme_w6bn7")),
          "HoverTheme" -> Variant.Object(ObjectValue.SubResource("Theme_cpxna"))
        )
      ),
      NodeData(
        "Label",
        None,
        Some("UiRoot/Menu2/MarginContainer/VBoxContainer/ItemsItem/PanelContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map("text" -> Variant.String("キャンプ"))
      ),
      NodeData(
        "BreakItem",
        None,
        Some("UiRoot/Menu2/MarginContainer/VBoxContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        Some("7_usk6j"),
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "unique_name_in_owner" -> Variant.Bool(true),
          "layout_mode" -> Variant.Int(2L),
          "theme" -> Variant.Object(ObjectValue.SubResource("Theme_2xomn")),
          "HoverTheme" -> Variant.Object(ObjectValue.SubResource("Theme_xcjvy"))
        )
      ),
      NodeData(
        "Label",
        None,
        Some("UiRoot/Menu2/MarginContainer/VBoxContainer/BreakItem/PanelContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map("text" -> Variant.String("脱出する"))
      ),
      NodeData(
        "Sprite2D",
        Some("Sprite2D"),
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "z_index" -> Variant.Int(-10L),
          "texture" -> Variant.Object(ObjectValue.ExtResource("7_k8514")),
          "centered" -> Variant.Bool(false)
        )
      ),
      NodeData(
        "TalkOverlay",
        Some("PanelContainer"),
        Some("."),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "z_index" -> Variant.Int(24L),
          "anchors_preset" -> Variant.Int(15L),
          "anchor_right" -> Variant.Float(1.0),
          "anchor_bottom" -> Variant.Float(1.0),
          "offset_right" -> Variant.Float(1280.0),
          "offset_bottom" -> Variant.Float(720.0),
          "grow_horizontal" -> Variant.Int(2L),
          "grow_vertical" -> Variant.Int(2L),
          "mouse_filter" -> Variant.Int(2L),
          "theme_override_styles/panel" -> Variant.Object(ObjectValue.SubResource("StyleBoxEmpty_w4yhy"))
        )
      ),
      NodeData(
        "TalkToHide",
        Some("PanelContainer"),
        Some("TalkOverlay"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "unique_name_in_owner" -> Variant.Bool(true),
          "clip_contents" -> Variant.Bool(true),
          "layout_mode" -> Variant.Int(2L),
          "size_flags_vertical" -> Variant.Int(8L),
          "theme_override_styles/panel" -> Variant.Object(ObjectValue.SubResource("StyleBoxFlat_yb3xh"))
        )
      ),
      NodeData(
        "MarginContainer",
        Some("MarginContainer"),
        Some("TalkOverlay/TalkToHide"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "clip_contents" -> Variant.Bool(true),
          "layout_mode" -> Variant.Int(2L),
          "theme_override_constants/margin_top" -> Variant.Int(10L),
          "theme_override_constants/margin_bottom" -> Variant.Int(10L)
        )
      ),
      NodeData(
        "VBoxContainer",
        Some("VBoxContainer"),
        Some("TalkOverlay/TalkToHide/MarginContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "layout_mode" -> Variant.Int(2L),
          "size_flags_horizontal" -> Variant.Int(4L)
        )
      ),
      NodeData(
        "NameTagArea",
        Some("MarginContainer"),
        Some("TalkOverlay/TalkToHide/MarginContainer/VBoxContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "layout_mode" -> Variant.Int(2L),
          "theme_override_constants/margin_bottom" -> Variant.Int(-10L)
        )
      ),
      NodeData(
        "PanelContainer",
        Some("PanelContainer"),
        Some("TalkOverlay/TalkToHide/MarginContainer/VBoxContainer/NameTagArea"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "layout_mode" -> Variant.Int(2L),
          "size_flags_horizontal" -> Variant.Int(0L),
          "theme_override_styles/panel" -> Variant.Object(ObjectValue.ExtResource("7_qberl"))
        )
      ),
      NodeData(
        "MarginContainer",
        Some("MarginContainer"),
        Some("TalkOverlay/TalkToHide/MarginContainer/VBoxContainer/NameTagArea/PanelContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "layout_mode" -> Variant.Int(2L),
          "theme_override_constants/margin_left" -> Variant.Int(10L),
          "theme_override_constants/margin_right" -> Variant.Int(10L)
        )
      ),
      NodeData(
        "TalkerName",
        Some("Label"),
        Some("TalkOverlay/TalkToHide/MarginContainer/VBoxContainer/NameTagArea/PanelContainer/MarginContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "unique_name_in_owner" -> Variant.Bool(true),
          "layout_mode" -> Variant.Int(2L),
          "text" -> Variant.String("リリアン"),
          "label_settings" -> Variant.Object(ObjectValue.ExtResource("11_prhjf"))
        )
      ),
      NodeData(
        "TalkPanel",
        Some("PanelContainer"),
        Some("TalkOverlay/TalkToHide/MarginContainer/VBoxContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "clip_contents" -> Variant.Bool(true),
          "custom_minimum_size" -> Variant.Vector2(600.0, 135.0),
          "layout_mode" -> Variant.Int(2L),
          "size_flags_horizontal" -> Variant.Int(4L),
          "size_flags_vertical" -> Variant.Int(4L),
          "theme_override_styles/panel" -> Variant.Object(ObjectValue.ExtResource("7_qberl"))
        )
      ),
      NodeData(
        "MarginContainer",
        Some("MarginContainer"),
        Some("TalkOverlay/TalkToHide/MarginContainer/VBoxContainer/TalkPanel"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "clip_contents" -> Variant.Bool(true),
          "layout_mode" -> Variant.Int(2L),
          "theme_override_constants/margin_left" -> Variant.Int(30L),
          "theme_override_constants/margin_top" -> Variant.Int(10L),
          "theme_override_constants/margin_right" -> Variant.Int(30L),
          "theme_override_constants/margin_bottom" -> Variant.Int(10L)
        )
      ),
      NodeData(
        "TalkBody",
        Some("Label"),
        Some("TalkOverlay/TalkToHide/MarginContainer/VBoxContainer/TalkPanel/MarginContainer"),
        Vector.empty,
        None,
        Vector.empty,
        None,
        None,
        None,
        None,
        Vector.empty,
        Vector.empty,
        Map(
          "unique_name_in_owner" -> Variant.Bool(true),
          "clip_contents" -> Variant.Bool(true),
          "layout_mode" -> Variant.Int(2L),
          "size_flags_vertical" -> Variant.Int(0L),
          "text" -> Variant.String("それ、チョコレート？\n「シュガー・ウォーズ」のプロモグッズと\nパッケージが似てるかも。"),
          "label_settings" -> Variant.Object(ObjectValue.ExtResource("11_prhjf")),
          "vertical_alignment" -> Variant.Int(1L)
        )
      )
    ),
    connections = Vector.empty,
    editableInstances = Vector(
      "UiRoot/Menu/Member",
      "UiRoot/Menu2/MarginContainer/VBoxContainer/MemberItem",
      "UiRoot/Menu2/MarginContainer/VBoxContainer/ItemsItem",
      "UiRoot/Menu2/MarginContainer/VBoxContainer/BreakItem"
    )
  )

  test("it should correctly parse everything from this script") {
    Vector(
      (tscnFirst, first),
      (tscnSecond, second),
      (tscnThird, third),
      (tscnFourth, fourth),
      (tscnAdvanced, advanced)
    ).map { case (tscn, packedScene) =>
      Parser.parseTscn(tscn) match {
        case Left(err) =>
          pprint.pprintln(err)
          assert(false)
        case Right(success) =>
          assertEquals(success, packedScene)
      }
    }
  }
}
