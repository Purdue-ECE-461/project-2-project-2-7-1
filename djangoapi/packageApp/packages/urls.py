from django.contrib import admin
from django.urls import path
from packages import views

urlpatterns = [
    path('project2/package/<id>', views.package_element),
    path('project2/authenticate', views.obtain_expiring_auth_token),
    path('project2/reset', views.db_reset),
    path('project2/package', views.package_create),
    path('project2/package/<id>/rate', views.package_rate)
]