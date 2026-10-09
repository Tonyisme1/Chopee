import React from 'react';
import { Outlet } from 'react-router-dom';
import { Header } from '../components/Header';
import { Footer } from '../components/Footer';

export const MarketLayout: React.FC = () => {
  return (
    <div className="min-h-screen flex flex-col bg-gray-50 text-gray-900 font-sans">
      <Header />
      <main className="flex-1 w-full max-w-7xl mx-auto px-4 py-6">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
};
export default MarketLayout;
