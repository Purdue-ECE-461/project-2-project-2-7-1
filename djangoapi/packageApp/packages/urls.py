from django.contrib import admin
from django.urls import path
from packages import views

urlpatterns = [
    path('package/<id>', views.package_element),
    path('authenticate', views.testauthtoken),
    path('reset', views.db_reset),
    path('package', views.package_create),
    path('package/<id>/rate', views.package_rate),
]