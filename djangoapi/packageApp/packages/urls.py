from django.contrib import admin
from django.urls import path
from packages import views

urlpatterns = [
    path('project2/package/<id>', views.package_element),
    #path('project2/package/byName/<name>', views.package_byName),
    path('project2/authenticate', views.auth_put),
    path('project2/reset', views.db_reset),
    #path('project2/packages?offset=2'),
    path('project2/package', views.package_create),
    path('project2/package/<id>/rate', views.package_rate)
]