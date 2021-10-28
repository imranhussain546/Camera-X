package com.imran.totalityassignment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import com.imran.totalityassignment.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = new Intent(MainActivity.this,HomeActivity.class);
        startActivity(intent);
    }

    public void addFragment(int replaceId, Fragment fragment) {
        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(replaceId, fragment);
            ft.commit();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public void replaceFragment(int replaceId, Fragment replaceFragment)
    {
        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(replaceId,replaceFragment);
            ft.addToBackStack(null);
            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}